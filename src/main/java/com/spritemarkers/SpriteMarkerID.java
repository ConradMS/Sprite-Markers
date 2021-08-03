package com.spritemarkers;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpriteMarkerID {
    private int RegionId;
    private int RegionX;
    private int RegionY;
    private int plane;
    private int spriteId;
    private int scale;

    public SpriteMarkerID (int regionId, int regionX, int regionY, int plane, int spriteId, int scale)
    {
        setRegionId(regionId);
        setRegionX(regionX);
        setRegionY(regionY);
        setPlane(plane);
        setSpriteId(spriteId);
        setScale(scale);
    }

    public boolean equals(SpriteMarkerID spriteMarkerID)
    {
        return spriteMarkerID.getRegionId() == this.getRegionId() &&
                spriteMarkerID.getRegionX() == this.getRegionX() &&
                spriteMarkerID.getRegionY() == this.getRegionY() &&
                spriteMarkerID.getPlane() == this.getPlane();
    }
}