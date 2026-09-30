package com.alchbuddy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AlchMathTest
{
	@Test
	public void calculatesLowAlchAtFortyPercent()
	{
		assertEquals(40L, AlchMath.lowAlchValue(100));
		assertEquals(0L, AlchMath.lowAlchValue(1));
		assertEquals(41L, AlchMath.lowAlchValue(104));
	}

	@Test
	public void calculatesProfitAfterNatureRune()
	{
		assertEquals(100L, AlchMath.profit(1_000L, 800L, 100L));
		assertEquals(-50L, AlchMath.profit(1_000L, 950L, 100L));
	}
}
