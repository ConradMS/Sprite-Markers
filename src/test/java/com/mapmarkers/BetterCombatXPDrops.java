package com.mapmarkers;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BetterCombatXPDrops
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BetterCombatXPDropsPlugin.class);
		RuneLite.main(args);
	}
}