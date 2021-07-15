package com.mapmarkers;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;

public class TileHighlighterOverlay extends Overlay{

    private final Client client;
    private final BetterCombatXPDropsPlugin plugin;
    private final BetterCombatXPDropsConfig config;

    private static final int MAX_TILES = 32;

    @Inject
    public TileHighlighterOverlay(Client client, BetterCombatXPDropsConfig config, BetterCombatXPDropsPlugin plugin) {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.MED);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        if(!config.showSprites() || !config.highlightSprites()) {
            return null;
        }

        final ArrayList<SpriteMarker> spriteMarkers = plugin.getSpriteMarkers();

        if(spriteMarkers.isEmpty()){
            return null;
        }

        for(SpriteMarker spriteMarker : spriteMarkers) {
            WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();

            if(playerLoc.distanceTo(spriteMarker.getWorldPoint()) <= MAX_TILES){
                Polygon tilePolygon = Perspective.getCanvasTilePoly(client, spriteMarker.getLocalPoint());
                if(tilePolygon != null){
                    OverlayUtil.renderPolygon(graphics, tilePolygon, Color.WHITE);
                }
            }
        }
        return null;
    }
}
