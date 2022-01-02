package com.spritemarkers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("spriteMarkers")
public interface SpriteMarkersConfig extends Config
{
	String SHOW_IMPORT = "show import";
	@ConfigItem(
			position = 1,
			keyName = "showSprites",
			name = "Display Sprites",
			description = "Hides/Shows ground sprites"
	)
	default boolean showSprites()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "spriteId",
			name = "Sprite ID",
			description = "What Sprite Id will be added"
	)
	default int spriteID()
	{
		return 1;
	}

	@ConfigItem(
			position = 3,
			keyName = "scale",
			name = "Sprite Scale",
			description = "The Scale of the sprite as a percent"
	)
	default int scale()
	{
		return 100;
	}

	@ConfigItem(
			position = 4,
			keyName = "showOnMap",
			name = "Show On Map",
			description = "Show the sprite on the minimap or not"
	)
	default boolean showOnMap()
	{
		return false;
	}

	@ConfigItem(
			position = 5,
			keyName = "smallMapSpriteSize",
			name = "Small Sprite Map Size",
			description = "The size that small sprites should be rendered on the minimap"
	)
	default int smallMapSpriteSize() {return 8; }

	@ConfigItem(
			position = 6,
			keyName = "largeMapSpriteSize",
			name = "Large Sprite Map Size",
			description = "The size that large sprites should be rendered on the minimap"
	)
	default int largeMapSpriteSize() {return 16; }

	@ConfigItem(
			position = 7,
			keyName = "highlightSprites",
			name = "Show Sprite Tiles",
			description = "Highlight the tiles sprites on to edit easier"
	)
	default boolean highlightSprites() {return false; }


	@ConfigItem(
			position = 8,
			keyName = "clear",
			name = "Clear Sprites",
			description = "Type clear to clear all loaded sprites"
	)
	default String clear()
	{
		return "------";
	}

	@ConfigItem(
			position = 9,
			keyName = SHOW_IMPORT,
			name = "Show Import/Export",
			description = "Show the import and export options under the run minimap orb"
	)
	default boolean showImport() {return false;}
}
