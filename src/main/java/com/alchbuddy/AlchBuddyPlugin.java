package com.alchbuddy;

import com.google.inject.Provides;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.item.ItemPrice;

@PluginDescriptor(
	name = "Alch Buddy",
	description = "Highlights profitable high-alch items and compares alchemy market metrics",
	tags = {"alchemy", "alch", "profit", "grand exchange", "bank", "inventory"},
	enabledByDefault = false
)
public class AlchBuddyPlugin extends Plugin
{
	static final int MAX_RESULTS = 250;
	private static final int PRICE_REFRESH_TICKS = 10;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AlchBuddyOverlay overlay;

	private AlchBuddyPanel panel;
	private NavigationButton navigationButton;
	private int ticksUntilPriceRefresh;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		panel = new AlchBuddyPanel(this);
		navigationButton = NavigationButton.builder()
			.tooltip("Alch Buddy")
			.icon(createIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigationButton);
		updateNatureRunePrice();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
		}
		panel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (--ticksUntilPriceRefresh <= 0)
		{
			ticksUntilPriceRefresh = PRICE_REFRESH_TICKS;
			updateNatureRunePrice();
		}
	}

	boolean isProfitable(int itemId)
	{
		int canonicalId = itemManager.canonicalize(itemId);
		ItemComposition item = itemManager.getItemComposition(canonicalId);
		long gePrice = itemManager.getItemPrice(canonicalId);
		long natureRunePrice = getNatureRunePrice();
		if (!item.isGeTradeable() || item.getHaPrice() <= 0 || gePrice <= 0 || natureRunePrice <= 0)
		{
			return false;
		}
		return AlchMath.profit(item.getHaPrice(), gePrice, natureRunePrice) > 0;
	}

	void search(String query, int requestId)
	{
		clientThread.invoke(() ->
		{
			long natureRunePrice = getNatureRunePrice();
			List<AlchItem> items = buildSearchResults(query, natureRunePrice);
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.showResults(requestId, natureRunePrice, items);
				}
			});
		});
	}

	private List<AlchItem> buildSearchResults(String query, long natureRunePrice)
	{
		List<AlchItem> items = new ArrayList<>();
		Set<Integer> seen = new HashSet<>();
		for (ItemPrice searchResult : itemManager.search(query))
		{
			int itemId = itemManager.canonicalize(searchResult.getId());
			if (!seen.add(itemId))
			{
				continue;
			}

			ItemComposition item = itemManager.getItemComposition(itemId);
			if (!item.isGeTradeable())
			{
				continue;
			}

			long gePrice = itemManager.getItemPrice(itemId);
			long highAlch = item.getHaPrice();
			long lowAlch = AlchMath.lowAlchValue(item.getPrice());
			ItemStats stats = itemManager.getItemStats(itemId);
			Integer buyLimit = stats == null || stats.getGeLimit() <= 0 ? null : stats.getGeLimit();
			long profit = gePrice > 0 && natureRunePrice > 0
				? AlchMath.profit(highAlch, gePrice, natureRunePrice)
				: 0;
			items.add(new AlchItem(itemId, item.getMembersName(), gePrice, lowAlch, highAlch, buyLimit, profit));
		}

		items.sort(Comparator.comparing(item -> item.name, String.CASE_INSENSITIVE_ORDER));
		return items.size() > MAX_RESULTS
			? new ArrayList<>(items.subList(0, MAX_RESULTS))
			: items;
	}

	private long getNatureRunePrice()
	{
		return itemManager.getItemPrice(ItemID.NATURERUNE);
	}

	private void updateNatureRunePrice()
	{
		long price = getNatureRunePrice();
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.setNatureRunePrice(price);
			}
		});
	}

	private static BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(235, 190, 45));
		graphics.fillOval(3, 3, 10, 10);
		graphics.setColor(new Color(100, 70, 15));
		graphics.setStroke(new BasicStroke(1.5f));
		graphics.drawOval(3, 3, 10, 10);
		graphics.drawString("A", 5, 12);
		graphics.dispose();
		return image;
	}

	@Provides
	AlchBuddyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AlchBuddyConfig.class);
	}
}
