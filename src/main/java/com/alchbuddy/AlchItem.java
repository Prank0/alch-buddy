package com.alchbuddy;

final class AlchItem
{
	final int id;
	final String name;
	final long gePrice;
	final long lowAlch;
	final long highAlch;
	final Integer buyLimit;
	final long profit;

	AlchItem(int id, String name, long gePrice, long lowAlch, long highAlch, Integer buyLimit, long profit)
	{
		this.id = id;
		this.name = name;
		this.gePrice = gePrice;
		this.lowAlch = lowAlch;
		this.highAlch = highAlch;
		this.buyLimit = buyLimit;
		this.profit = profit;
	}
}
