package com.spritemarkers;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
public class SpriteMarker
{
    int spriteId;
    int scale;
    WorldPoint worldPoint;

    public SpriteMarker(int spriteId, int scale, WorldPoint worldPoint)
    {
        this.spriteId = spriteId;
        this.scale = scale;
        this.worldPoint = worldPoint;
    }

    public String toString()
    {
        return this.worldPoint.toString();
    }
}
