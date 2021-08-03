package com.spritemarkers;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;

public class TileHighlighterOverlay extends Overlay
{

    private final Client client;
    private final SpriteMarkersPlugin plugin;
    private final SpriteMarkersConfig config;

    private static final int MAX_TILES = 32;

    @Inject
    public TileHighlighterOverlay(Client client, SpriteMarkersConfig config, SpriteMarkersPlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.MED);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

        if(!config.showSprites() || !config.highlightSprites())
        {
            return null;
        }

        final ArrayList<SpriteMarker> spriteMarkers = plugin.getSpriteMarkersLoaded();

        if(spriteMarkers.isEmpty())
        {
            return null;
        }

        for(SpriteMarker spriteMarker : spriteMarkers)
        {
            WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();

            if(playerLoc.distanceTo(spriteMarker.getWorldPoint()) <= MAX_TILES)
            {
                WorldPoint worldLoc = spriteMarker.getWorldPoint();
                LocalPoint localPoint = LocalPoint.fromWorld(client, worldLoc);

                if(localPoint == null)
                {
                    return null;
                }

                Polygon tilePolygon = Perspective.getCanvasTilePoly(client, localPoint);
                if(tilePolygon != null)
                {
                    OverlayUtil.renderPolygon(graphics, tilePolygon, Color.WHITE);
                }
            }
        }
        return null;
    }
}
