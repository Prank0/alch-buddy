package com.alchbuddy;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AlchBuddyPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AlchBuddyPlugin.class);
		RuneLite.main(args);
	}
}
