package com.spritemarkers;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MinimapSpriteOverlay extends Overlay
{

    private final Client client;
    private final SpriteMarkersPlugin plugin;
    private final SpriteMarkersConfig config;

    private final static int MINIMAP_RADIUS = 16;
    private final static int BIG_SPRITE = 250;

    @Inject
    SpriteManager spriteManager;

    @Inject
    public MinimapSpriteOverlay(Client client, SpriteMarkersConfig config, SpriteMarkersPlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

        if(!config.showSprites() || !config.showOnMap())
        {
            return null;
        }

        final ArrayList<SpriteMarker> spriteMarkers = plugin.getSpriteMarkers();

        if(spriteMarkers.isEmpty())
        {
            return null;
        }

        for(SpriteMarker spriteMarker : spriteMarkers)
        {
            drawMinimapSprite(graphics, spriteMarker);
        }
        return null;
    }

    private void drawMinimapSprite(Graphics2D graphics, SpriteMarker spriteMarker)
    {
        WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();

        if(playerLoc != null && playerLoc.distanceTo(spriteMarker.getWorldPoint()) <= MINIMAP_RADIUS)
        {
            Point miniMapPoint = Perspective.localToMinimap(client, spriteMarker.getLocalPoint());

            if(miniMapPoint == null)
            {
                return;
            }

            BufferedImage toImage =  spriteManager.getSprite(1, 0);

            try
            {
                toImage = spriteManager.getSprite(spriteMarker.getSpriteId(), 0);
            } catch (IllegalArgumentException e)
            {
                // not a sprite
            }

            if(toImage != null)
            {
                int scaleFactor = Math.min(toImage.getHeight(), toImage.getWidth()) / config.largeMapSpriteSize();

                if(Math.max(toImage.getHeight(), toImage.getWidth()) < BIG_SPRITE)
                {
                    scaleFactor = Math.min(toImage.getHeight(), toImage.getWidth()) / config.smallMapSpriteSize();
                }

                scaleFactor = Math.max(scaleFactor, 1);

                toImage = ImageUtil.resizeImage(toImage, toImage.getWidth() / scaleFactor, toImage.getHeight() / scaleFactor);
                Point offsetPoint = new Point(miniMapPoint.getX() - toImage.getWidth() / 2, miniMapPoint.getY() - toImage.getHeight() / 2);
                OverlayUtil.renderImageLocation(graphics, offsetPoint, toImage);
            }
        }
    }
}
