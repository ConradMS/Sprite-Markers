package com.mapmarkers;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
public class SpriteMarker {
    int regionId;
    int spriteId;
    int scale;
    LocalPoint localPoint;
    WorldPoint worldPoint;

    public SpriteMarker(int regionId, int spriteId, int scale, LocalPoint localPoint, WorldPoint worldPoint) {
        this.regionId = regionId;
        this.spriteId = spriteId;
        this.scale = scale;
        this.localPoint = localPoint;
        this.worldPoint = worldPoint;
    }
}
