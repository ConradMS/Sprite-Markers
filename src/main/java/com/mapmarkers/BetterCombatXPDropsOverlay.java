package com.mapmarkers;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemMapping;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.SpriteOverride;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.util.ImageUtil;
import sun.jvm.hotspot.debugger.cdbg.Sym;

public class BetterCombatXPDropsOverlay extends Overlay{

    private final Client client;
    private final BetterCombatXPDropsPlugin plugin;
    private final BetterCombatXPDropsConfig config;

    private static final int MAX_TILES = 32;

    @Inject
    SpriteManager spriteManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    public BetterCombatXPDropsOverlay(Client client, BetterCombatXPDropsConfig config, BetterCombatXPDropsPlugin plugin) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(!(config.showSprites())) {
            return null;
        }
        final ArrayList<SpriteMarker> spriteMarkers = plugin.getSpriteMarkers();
        if (spriteMarkers.isEmpty()) {
            return null;
        }

        for(final SpriteMarker spriteMarker : spriteMarkers) {
            BufferedImage toImage =  spriteManager.getSprite(1, 0);

            try {
                toImage = spriteManager.getSprite(spriteMarker.getSpriteId(), 0);
            } catch (IllegalArgumentException e) {
                // not a sprite
            }

            WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();

            if(toImage != null && spriteMarker.worldPoint.distanceTo(playerLoc) <= MAX_TILES){
                final LocalPoint locPoint = spriteMarker.getLocalPoint();
                toImage = ImageUtil.resizeImage(toImage, toImage.getWidth() * spriteMarker.getScale() / 100, toImage.getHeight() * spriteMarker.getScale() / 100);
                OverlayUtil.renderImageLocation(client, graphics, locPoint, toImage, 0);
            }
        }

        if (config.clear().equals("clear")) {

            for(SpriteMarker spriteMarker : spriteMarkers) {
                if(configManager.getConfiguration(BetterCombatXPDropsPlugin.CONFIGGROUP, BetterCombatXPDropsPlugin.REGION + spriteMarker.regionId) != null)
                    configManager.unsetConfiguration(BetterCombatXPDropsPlugin.CONFIGGROUP, BetterCombatXPDropsPlugin.REGION + spriteMarker.regionId);
            }

            plugin.getSpriteMarkers().clear();
            plugin.getWorldLocations().clear();
        }

        return null;
    }
}
