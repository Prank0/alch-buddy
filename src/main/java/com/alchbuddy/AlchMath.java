package com.alchbuddy;

final class AlchMath
{
	private AlchMath()
	{
	}

	static long lowAlchValue(int storePrice)
	{
		return Math.max(0L, (long) storePrice * 2L / 5L);
	}

	static long profit(long highAlchValue, long gePrice, long natureRunePrice)
	{
		return highAlchValue - gePrice - natureRunePrice;
	}
}
