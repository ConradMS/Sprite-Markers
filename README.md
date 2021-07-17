# Sprite Markers
Allows you to mark tiles with sprites by **holding down left control and right-clicking on a tile**. You can remove sprites in the same way.
### Settings
**Sprite ID:** The ID of the sprite you want to mark tiles with. Most sprite IDS can be found [here](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/SpriteID.java). Some useful ids include: 
* 15 - 111, 319 - 324, 349 - 365 (standard spells)
* 115 - 135, 502 - 505, 945 - 947, 1420, 1421 (Prayers)
* 197 - 221 (Skills)
* 325 - 348 (Ancients)
* 543 - 586 (Lunar spell book)

**Scale:** Scale of added sprite as a percentage of the original size, e.g scale = 70 results in the sprite scaled down to 70% of its original size.

**Small size sprite / Large size sprite:** The rough sizes that small and large sprites respectively should be drawn on the map

**Clear:** Type clear to clear all currently loaded sprites.

Note that each sprite will save its own sprite Id and scale and that the small size sprite / large size sprite is uniform for all sprites.

### Some of the above sprites in game
![Some of the above sprites](/assets/ExampleSprites.png)

### Scaled sprite and another random sprite in game and on minimap
![Scaled sprite and large sprite](/assets/Example2.PNG)

![Sprites on Minimap](/assets/Minimap%20Example.png)