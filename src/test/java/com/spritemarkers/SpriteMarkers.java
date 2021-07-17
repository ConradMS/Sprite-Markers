package com.spritemarkers;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SpriteMarkers
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SpriteMarkersPlugin.class);
		RuneLite.main(args);
	}
}