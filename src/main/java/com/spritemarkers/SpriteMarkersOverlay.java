package com.spritemarkers;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.ImageUtil;

public class SpriteMarkersOverlay extends Overlay
{

    private final Client client;
    private final SpriteMarkersPlugin plugin;
    private final SpriteMarkersConfig config;

    private static final int MAX_TILES = 32;

    @Inject
    SpriteManager spriteManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    public SpriteMarkersOverlay(Client client, SpriteMarkersConfig config, SpriteMarkersPlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if(!(config.showSprites()))
        {
            return null;
        }
        final ArrayList<SpriteMarker> spriteMarkers = plugin.getSpriteMarkers();
        if (spriteMarkers.isEmpty())
        {
            return null;
        }

        for(final SpriteMarker spriteMarker : spriteMarkers)
        {
            BufferedImage toImage =  spriteManager.getSprite(1, 0);

            try
            {
                toImage = spriteManager.getSprite(spriteMarker.getSpriteId(), 0);
            } catch (IllegalArgumentException e)
            {
                // not a sprite
            }

            WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();

            if(toImage != null && spriteMarker.worldPoint.distanceTo(playerLoc) <= MAX_TILES)
            {
                final LocalPoint locPoint = spriteMarker.getLocalPoint();

                if (locPoint == null) {
                    return null;
                }

                toImage = ImageUtil.resizeImage(toImage, toImage.getWidth() * spriteMarker.getScale() / 100, toImage.getHeight() * spriteMarker.getScale() / 100);
                OverlayUtil.renderImageLocation(client, graphics, locPoint, toImage, 0);
            }
        }

        if (config.clear().equals("clear"))
        {

            for(SpriteMarker spriteMarker : spriteMarkers)
            {
                if(configManager.getConfiguration(SpriteMarkersPlugin.CONFIG_GROUP, SpriteMarkersPlugin.REGION + spriteMarker.regionId) != null)
                    configManager.unsetConfiguration(SpriteMarkersPlugin.CONFIG_GROUP, SpriteMarkersPlugin.REGION + spriteMarker.regionId);
            }

            plugin.getSpriteMarkers().clear();
            plugin.getWorldLocations().clear();
        }
        return null;
    }
}
