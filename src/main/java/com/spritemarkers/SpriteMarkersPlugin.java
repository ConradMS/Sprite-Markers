package com.spritemarkers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Tile;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuAction;
import net.runelite.api.GameState;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@PluginDescriptor(
		name = "Sprite Markers",
		description = "Mark tiles with sprites",
		tags = {"overlay", "tiles"}
)
public class SpriteMarkersPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SpriteMarkersConfig config;

	@Inject
	private SpriteMarkersOverlay overlay;

	@Inject
	private TileHighlighterOverlay highlighterOverlay;

	@Inject
	private MinimapSpriteOverlay minimapSpriteOverlay;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Gson gson;

	private final static int LEFT_CONTROL_KEYCODE = 82;
	private final static String ADD_SPRITE = "Add Sprite";
	private final static String REMOVE_SPRITE = "Remove Sprite";
	private final static String CANCEL = "Cancel";

	protected static final  String CONFIG_GROUP = "spriteMarkers";
	protected static final String REGION = "Region_";

	@Getter
	private final ArrayList<SpriteMarker> spriteMarkersLoaded = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		overlayManager.add(highlighterOverlay);
		overlayManager.add(minimapSpriteOverlay);
		loadSprites();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(highlighterOverlay);
		overlayManager.remove(minimapSpriteOverlay);
		spriteMarkersLoaded.clear();
	}

	private boolean containsSprite(ArrayList<SpriteMarkerID> spriteMarkerIDS, SpriteMarkerID targetSpriteMarker)
	{
		for(SpriteMarkerID spriteMarkerID : spriteMarkerIDS)
		{
			if(spriteMarkerID.equals(targetSpriteMarker))
			{
				return true;
			}
		}
		return false;
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryEvent)
	{
		final boolean markerKeyPressed = client.isKeyPressed(LEFT_CONTROL_KEYCODE);

		if (markerKeyPressed && menuEntryEvent.getOption().equals(CANCEL))
		{
			final Tile targetTile = client.getSelectedSceneTile();

			if(targetTile != null)
			{
				final LocalPoint localPoint = targetTile.getLocalLocation();
				final WorldPoint worldLoc = WorldPoint.fromLocalInstance(client, localPoint);
				final int regionID = worldLoc.getRegionID();
				final SpriteMarkerID targetSpriteMarkerID = new SpriteMarkerID(regionID,
						worldLoc.getRegionX(), worldLoc.getRegionY(), client.getPlane(),
						config.spriteID(), config.scale());

				final ArrayList<SpriteMarkerID> savedSprites = jsonToSprite(regionID);
				final boolean spriteHere = containsSprite(savedSprites, targetSpriteMarkerID);

				if (!spriteHere)
				{
					client.createMenuEntry(-1).setOption(ADD_SPRITE)
							.setTarget(menuEntryEvent.getTarget())
							.setType(MenuAction.RUNELITE)
							.onClick(e -> addTileSprite());
				} else
				{
					client.createMenuEntry(-1).setOption(REMOVE_SPRITE)
							.setTarget(menuEntryEvent.getTarget())
							.setType(MenuAction.RUNELITE)
							.onClick(e -> removeTileSprite());
				}
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			loadSprites();
		}
	}

	private void addTileSprite()
	{
		final Tile targetTile = client.getSelectedSceneTile();
		if (targetTile != null)
		{
			final LocalPoint localPoint = targetTile.getLocalLocation();
			final WorldPoint worldLoc = WorldPoint.fromLocalInstance(client, localPoint);
			final int regionID = worldLoc.getRegionID();
			SpriteMarkerID spriteMarkerID = new SpriteMarkerID(regionID, worldLoc.getRegionX(), worldLoc.getRegionY(),
					client.getPlane(), config.spriteID(), config.scale());
			saveSprite(regionID, spriteMarkerID);
			loadSprites();
		}

	}

	private void removeTileSprite()
	{
		final Tile tile = client.getSelectedSceneTile();

		if(tile == null)
		{
			return;
		}

		final LocalPoint localPoint = tile.getLocalLocation();
		final WorldPoint targetLocation = WorldPoint.fromLocalInstance(client, localPoint);
		final int regionID = targetLocation.getRegionID();
		final SpriteMarkerID TargetSpriteMarkerID = new SpriteMarkerID(regionID, targetLocation.getRegionX(),
				targetLocation.getRegionY(), client.getPlane(), config.spriteID(), config.scale());

		ArrayList<SpriteMarkerID> currSprites = jsonToSprite(regionID);

		for (SpriteMarkerID spriteMarkerID : currSprites)
		{
			if(spriteMarkerID.equals(TargetSpriteMarkerID))
			{
				currSprites.remove(spriteMarkerID);
				break;
			}
		}

		if(!currSprites.isEmpty())
		{
			String spriteMarkersToJson = gson.toJson(currSprites);
			configManager.setConfiguration(CONFIG_GROUP, REGION + regionID, spriteMarkersToJson);
		} else
		{
			configManager.unsetConfiguration(CONFIG_GROUP, REGION + regionID);
		}

		loadSprites();
	}

	void saveSprite(int regionId, SpriteMarkerID newSprite)
	{
		ArrayList<SpriteMarkerID> currPoints = jsonToSprite(regionId);
		currPoints.add(newSprite);

		String spriteMarkersToJson = gson.toJson(currPoints);
		configManager.setConfiguration(CONFIG_GROUP, REGION + regionId, spriteMarkersToJson);
	}

	void loadSprites()
	{
		spriteMarkersLoaded.clear();
		int[] loadedRegions = client.getMapRegions();

		if(loadedRegions != null)
		{
			for(int loadedRegion : loadedRegions)
			{
				ArrayList<SpriteMarkerID> regionSprites = jsonToSprite(loadedRegion);
				ArrayList<SpriteMarker> spriteMarkers = getSpriteMarkers(regionSprites);
				spriteMarkersLoaded.addAll(spriteMarkers);
			}
		}
	}

	ArrayList<SpriteMarkerID> jsonToSprite(int regionId)
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, REGION + regionId);
		if(json != null && !json.equals(""))
		{
			return gson.fromJson(json, new TypeToken<ArrayList<SpriteMarkerID>>(){}.getType());
		} else
		{
			return new ArrayList<SpriteMarkerID>();
		}
	}

	private ArrayList<SpriteMarker> getSpriteMarkers(ArrayList<SpriteMarkerID> regionSprites)
	{
		if(regionSprites.isEmpty())
		{
			return new ArrayList<SpriteMarker>();
		}
		ArrayList<SpriteMarker> spriteMarkers = new ArrayList<SpriteMarker>();

		for (SpriteMarkerID spriteMarkerID : regionSprites)
		{
			WorldPoint worldLoc = WorldPoint.fromRegion(spriteMarkerID.getRegionId(), spriteMarkerID.getRegionX(),
					spriteMarkerID.getRegionY(), spriteMarkerID.getPlane());

			SpriteMarker spriteMarker = new SpriteMarker(spriteMarkerID.getSpriteId(), spriteMarkerID.getScale(), worldLoc);
			spriteMarkers.add(spriteMarker);
			final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, spriteMarker.getWorldPoint());

			for(WorldPoint worldPoint : localWorldPoints)
			{
				spriteMarkers.add(new SpriteMarker(spriteMarkerID.getSpriteId(), spriteMarkerID.getScale(), worldPoint));
			}
		}

		return spriteMarkers;
	}


	@Provides
	SpriteMarkersConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpriteMarkersConfig.class);
	}
}
