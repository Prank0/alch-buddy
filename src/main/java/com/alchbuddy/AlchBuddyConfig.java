package com.alchbuddy;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("alch-buddy")
public interface AlchBuddyConfig extends Config
{
	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color used for profitable bank and inventory items"
	)
	default Color highlightColor()
	{
		return new Color(40, 220, 100, 180);
	}
}
