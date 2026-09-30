package com.alchbuddy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

final class AlchBuddyPanel extends PluginPanel
{
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

	private final AlchBuddyPlugin plugin;
	private final JTextField searchField = new JTextField();
	private final JLabel naturePrice = new JLabel("Nature rune: loading…");
	private final JLabel status = new JLabel("Loading items…");
	private final AlchTableModel tableModel = new AlchTableModel();
	private final JTable table = new JTable(tableModel);
	private final Timer searchTimer;
	private int requestId;

	AlchBuddyPanel(AlchBuddyPlugin plugin)
	{
		super(false);
		this.plugin = plugin;
		setLayout(new BorderLayout(0, 6));
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel header = new JPanel(new BorderLayout(0, 4));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JLabel title = new JLabel("Alch Buddy");
		title.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		header.add(title, BorderLayout.NORTH);
		header.add(naturePrice, BorderLayout.CENTER);
		searchField.setToolTipText("Search tradeable items by name");
		header.add(searchField, BorderLayout.SOUTH);
		add(header, BorderLayout.NORTH);

		configureTable();
		add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel footer = new JPanel(new BorderLayout(4, 0));
		footer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		footer.add(status, BorderLayout.CENTER);
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener(event -> runSearch());
		JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonHolder.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttonHolder.add(refresh);
		footer.add(buttonHolder, BorderLayout.EAST);
		add(footer, BorderLayout.SOUTH);

		searchTimer = new Timer(250, event -> runSearch());
		searchTimer.setRepeats(false);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent event)
			{
				scheduleSearch();
			}

			@Override
			public void removeUpdate(DocumentEvent event)
			{
				scheduleSearch();
			}

			@Override
			public void changedUpdate(DocumentEvent event)
			{
				scheduleSearch();
			}
		});
	}

	private void configureTable()
	{
		table.setAutoCreateRowSorter(false);
		TableRowSorter<AlchTableModel> sorter = new TableRowSorter<>(tableModel);
		sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(5, SortOrder.DESCENDING)));
		table.setRowSorter(sorter);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setFillsViewportHeight(true);
		table.setToolTipText("Click a column header to sort");

		NumberRenderer numberRenderer = new NumberRenderer();
		TableColumnModel columns = table.getColumnModel();
		columns.getColumn(0).setPreferredWidth(145);
		for (int index = 1; index < columns.getColumnCount(); index++)
		{
			columns.getColumn(index).setPreferredWidth(index == 4 ? 70 : 85);
			columns.getColumn(index).setCellRenderer(numberRenderer);
		}
	}

	private void scheduleSearch()
	{
		searchTimer.restart();
	}

	private void runSearch()
	{
		String query = searchField.getText().trim();
		int currentRequest = ++requestId;
		status.setText("Searching…");
		plugin.search(query, currentRequest);
	}

	void refresh()
	{
		runSearch();
	}

	void showResults(int completedRequest, long natureRunePrice, List<AlchItem> items)
	{
		assert SwingUtilities.isEventDispatchThread();
		if (completedRequest != requestId)
		{
			return;
		}
		setNatureRunePrice(natureRunePrice);
		tableModel.setItems(items);
		status.setText(items.size() + " results");
	}

	void setNatureRunePrice(long price)
	{
		assert SwingUtilities.isEventDispatchThread();
		naturePrice.setText(price > 0
			? "Nature rune: " + NUMBER_FORMAT.format(price) + " gp"
			: "Nature rune: price unavailable");
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(PluginPanel.PANEL_WIDTH + PluginPanel.SCROLLBAR_WIDTH, 0);
	}

	private static final class AlchTableModel extends AbstractTableModel
	{
		private static final String[] COLUMNS = {"Item", "GE", "Low alch", "High alch", "Buy limit", "Profit"};
		private List<AlchItem> items = new ArrayList<>();

		void setItems(List<AlchItem> items)
		{
			this.items = new ArrayList<>(items);
			fireTableDataChanged();
		}

		@Override
		public int getRowCount()
		{
			return items.size();
		}

		@Override
		public int getColumnCount()
		{
			return COLUMNS.length;
		}

		@Override
		public String getColumnName(int column)
		{
			return COLUMNS[column];
		}

		@Override
		public Class<?> getColumnClass(int column)
		{
			if (column == 0)
			{
				return String.class;
			}
			if (column == 4)
			{
				return Integer.class;
			}
			return Long.class;
		}

		@Override
		public Object getValueAt(int row, int column)
		{
			AlchItem item = items.get(row);
			switch (column)
			{
				case 0:
					return item.name;
				case 1:
					return item.gePrice;
				case 2:
					return item.lowAlch;
				case 3:
					return item.highAlch;
				case 4:
					return item.buyLimit;
				case 5:
					return item.profit;
				default:
					throw new IllegalArgumentException("Unknown column " + column);
			}
		}
	}

	private static final class NumberRenderer extends DefaultTableCellRenderer
	{
		NumberRenderer()
		{
			setHorizontalAlignment(SwingConstants.RIGHT);
		}

		@Override
		protected void setValue(Object value)
		{
			setText(value == null ? "—" : NUMBER_FORMAT.format(value));
		}
	}
}
