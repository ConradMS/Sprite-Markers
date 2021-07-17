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

	protected static final  String CONFIG_GROUP = "spriteMarkers";
	protected static final String REGION = "Region_";

	@Getter
	private final ArrayList<SpriteMarker> spriteMarkers = new ArrayList<>();

	@Getter
	private final ArrayList<WorldPoint> worldLocations = new ArrayList<>();

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
		spriteMarkers.clear();
		worldLocations.clear();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
	{
		final boolean markerKeyPressed = client.isKeyPressed(LEFT_CONTROL_KEYCODE);
		if (markerKeyPressed && menuEntryAdded.getOption().equals("Cancel"))
		{
			final Tile targetTile = client.getSelectedSceneTile();

			if(targetTile != null)
			{
				final boolean spriteHere = worldLocations.contains(targetTile.getWorldLocation());

				MenuEntry[] menuEntries = client.getMenuEntries();
				menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);
				MenuEntry defaultOption = new MenuEntry();

				if (!spriteHere)
				{
					defaultOption.setOption(ADD_SPRITE);
				} else
				{
					defaultOption.setOption(REMOVE_SPRITE);
				}

				defaultOption.setTarget(menuEntryAdded.getTarget());
				defaultOption.setType(MenuAction.RUNELITE.getId());
				menuEntries[menuEntries.length - 1] = defaultOption;

				client.setMenuEntries(menuEntries);
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

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if(menuOptionClicked.getMenuAction().getId() == MenuAction.RUNELITE.getId())
		{
			if(menuOptionClicked.getMenuOption().equals(ADD_SPRITE))
			{
				addTileSprite();
			}
			else if(menuOptionClicked.getMenuOption().equals(REMOVE_SPRITE))
			{
				removeTileSprite();
			}
		}
	}

	private void addTileSprite() {
		final Tile targetTile = client.getSelectedSceneTile();
		if (targetTile != null)
		{
			final WorldPoint worldLoc = targetTile.getWorldLocation();
			final LocalPoint tileSceneLocation = targetTile.getLocalLocation();

			if (tileSceneLocation != null && worldLoc != null)
			{
				worldLocations.add(worldLoc);
				spriteMarkers.add(new SpriteMarker(worldLoc.getRegionID(), config.spriteID(), config.scale(), tileSceneLocation, worldLoc));
			}

			if (worldLoc != null)
			{
				saveSprites(worldLoc.getRegionID());
			}
		}
	}

	private void removeTileSprite()
	{
		final Tile tile = client.getSelectedSceneTile();

		if(tile == null)
		{
			return;
		}

		final WorldPoint targetLocation = tile.getWorldLocation();

		for (final SpriteMarker spriteMarker : spriteMarkers)
		{
			final WorldPoint worldLoc = spriteMarker.getWorldPoint();

			if(worldLoc.equals(targetLocation))
			{
				spriteMarkers.remove(spriteMarker);
				saveSprites(targetLocation.getRegionID());
				worldLocations.remove(targetLocation);
				break;
			}
		}
	}

	void saveSprites(int regionId)
	{
		ArrayList<SpriteMarker> inRegionPoints = new ArrayList<>();
		for(SpriteMarker spriteMarker : spriteMarkers)
		{
			if(spriteMarker.getRegionId() == regionId)
			{
				inRegionPoints.add(spriteMarker);
			}
		}

		if(!inRegionPoints.isEmpty())
		{
			String spriteMarkersToJson = gson.toJson(inRegionPoints);
			configManager.setConfiguration(CONFIG_GROUP, REGION + regionId, spriteMarkersToJson);
		} else
		{
			configManager.unsetConfiguration(CONFIG_GROUP, REGION + regionId);
		}
	}

	void loadSprites()
	{
		spriteMarkers.clear();
		worldLocations.clear();
		int[] loadedRegions = client.getMapRegions();

		if(loadedRegions != null)
		{
			for(int loadedRegion : loadedRegions)
			{
				spriteMarkers.addAll(jsonToSprite(loadedRegion));
			}

			for(SpriteMarker spriteMarker : spriteMarkers)
			{
				final WorldPoint worldLoc = spriteMarker.getWorldPoint();
				spriteMarker.setLocalPoint(LocalPoint.fromWorld(client, worldLoc));
				worldLocations.add(worldLoc);
			}
		}
	}

	ArrayList<SpriteMarker> jsonToSprite(int regionId)
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, REGION + regionId);
		if(json != null && !json.equals(""))
		{
			return gson.fromJson(json, new TypeToken<ArrayList<SpriteMarker>>(){}.getType());
		} else
		{
			return new ArrayList<SpriteMarker>();
		}
	}

	@Provides
	SpriteMarkersConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpriteMarkersConfig.class);
	}
}
