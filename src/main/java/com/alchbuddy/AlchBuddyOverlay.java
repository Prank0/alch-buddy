package com.alchbuddy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

final class AlchBuddyOverlay extends WidgetItemOverlay
{
	private final AlchBuddyPlugin plugin;
	private final AlchBuddyConfig config;

	@Inject
	AlchBuddyOverlay(AlchBuddyPlugin plugin, AlchBuddyConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		showOnInventory();
		showOnBank();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (!plugin.isProfitable(itemId))
		{
			return;
		}

		Rectangle bounds = widgetItem.getCanvasBounds();
		Color color = config.highlightColor();
		Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(55, color.getAlpha()));
		Stroke oldStroke = graphics.getStroke();
		Color oldColor = graphics.getColor();
		graphics.setColor(fill);
		graphics.fill(bounds);
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2f));
		graphics.draw(bounds);
		graphics.setStroke(oldStroke);
		graphics.setColor(oldColor);
	}
}
