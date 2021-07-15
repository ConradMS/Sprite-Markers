package com.mapmarkers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
@PluginDescriptor(
	name = "Sprite Markers",
	description = "Mark tiles with sprites",
	tags = {"experience", "combat", "xpdrop"}
)
public class BetterCombatXPDropsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private BetterCombatXPDropsConfig config;

	@Inject
	private BetterCombatXPDropsOverlay overlay;

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

	private final int LEFTCONTROLKEYCODE = 82;
	private final String ADDSPRITE = "Add Sprite";
	private final String REMOVESPRITE = "Remove Sprite";
	private final String REFRESHSPRITE = "Refresh Sprite";

	protected static final  String CONFIGGROUP = "spriteMarkers";
	protected static final String REGION = "Region_";

	@Getter
	private final ArrayList<SpriteMarker> spriteMarkers = new ArrayList<SpriteMarker>();

	@Getter
	private final ArrayList<WorldPoint> worldLocations = new ArrayList<WorldPoint>();

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(overlay);
		overlayManager.add(highlighterOverlay);
		overlayManager.add(minimapSpriteOverlay);
		loadSprites();
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
		overlayManager.remove(highlighterOverlay);
		overlayManager.remove(minimapSpriteOverlay);
		spriteMarkers.clear();
		worldLocations.clear();
	}

//	@Subscribe
//	public void onGameTick(GameTick gameTick) {
//		System.out.println(configManager.getConfiguration(CONFIGGROUP, REGION + client.getLocalPlayer().getWorldLocation().getRegionID()));
//		System.out.println(client.getLocalPlayer().getWorldLocation().getRegionID());
//		configManager.unsetConfiguration(CONFIGGROUP, REGION + client.getLocalPlayer().getWorldLocation().getRegionID());
//	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded) {
		final boolean markerKeyPressed = client.isKeyPressed(LEFTCONTROLKEYCODE);
		if (markerKeyPressed && menuEntryAdded.getOption().equals("Cancel")){
			final Tile targetTile = client.getSelectedSceneTile();

			if(targetTile != null){
				final boolean spriteHere = worldLocations.contains(targetTile.getWorldLocation());

				MenuEntry[] menuEntries = client.getMenuEntries();
				menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);
				MenuEntry defaultOption = new MenuEntry();

				if (!spriteHere){
					defaultOption.setOption(ADDSPRITE);
				} else {
					defaultOption.setOption(REMOVESPRITE);
				}

				defaultOption.setTarget(menuEntryAdded.getTarget());
				defaultOption.setType(MenuAction.RUNELITE.getId());
				menuEntries[menuEntries.length - 1] = defaultOption;

				client.setMenuEntries(menuEntries);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			loadSprites();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked){
		if(menuOptionClicked.getMenuAction().getId() == MenuAction.RUNELITE.getId()) {
			if(menuOptionClicked.getMenuOption().equals(ADDSPRITE)) {
				addTileSprite();
			}
			else if(menuOptionClicked.getMenuOption().equals(REMOVESPRITE)) {
				removeTileSprite();
			}
		}
	}

	private void addTileSprite() {
		final Tile targetTile = client.getSelectedSceneTile();
		if (targetTile != null) {
			final WorldPoint worldLoc = targetTile.getWorldLocation();
			final LocalPoint tileSceneLocation = targetTile.getLocalLocation();
			if (tileSceneLocation != null && worldLoc != null) {
				worldLocations.add(worldLoc);
				spriteMarkers.add(new SpriteMarker(worldLoc.getRegionID(), config.spriteID(), config.scale(), tileSceneLocation, worldLoc));
			}
			if (worldLoc != null)
				saveSprites(worldLoc.getRegionID());
		}
	}

	private void removeTileSprite() {
		final WorldPoint targetLocation = client.getSelectedSceneTile().getWorldLocation();
		for (final SpriteMarker spriteMarker : spriteMarkers){
			final WorldPoint worldLoc = spriteMarker.getWorldPoint();
			if(worldLoc.equals(targetLocation)){
				spriteMarkers.remove(spriteMarker);
				saveSprites(targetLocation.getRegionID());
				worldLocations.remove(targetLocation);
				break;
			}
		}
	}

	void saveSprites(int regionId){
		if(!spriteMarkers.isEmpty()){
			String spriteMarkersToJson = gson.toJson(spriteMarkers);
			configManager.setConfiguration(CONFIGGROUP, REGION + regionId, spriteMarkersToJson);
		} else {
			configManager.unsetConfiguration(CONFIGGROUP, REGION + regionId);
		}
	}

	void loadSprites(){
		spriteMarkers.clear();
		worldLocations.clear();
		int[] loadedRegions = client.getMapRegions();

		if(loadedRegions != null){
			for(int loadedRegion : loadedRegions) {
				spriteMarkers.addAll(jsonToSprite(loadedRegion));
			}

			for(SpriteMarker spriteMarker : spriteMarkers) {
				final WorldPoint worldLoc = spriteMarker.getWorldPoint();
				spriteMarker.setLocalPoint(LocalPoint.fromWorld(client, worldLoc));
				worldLocations.add(worldLoc);
			}
		}
	}

	ArrayList<SpriteMarker> jsonToSprite(int regionId) {
		String json = configManager.getConfiguration(CONFIGGROUP, REGION + regionId);
		if(json != null && !json.equals("")) {
			return gson.fromJson(json, new TypeToken<ArrayList<SpriteMarker>>(){}.getType());
		} else {
			return new ArrayList<SpriteMarker>();
		}
	}

	@Provides
	BetterCombatXPDropsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(BetterCombatXPDropsConfig.class);
	}
}
