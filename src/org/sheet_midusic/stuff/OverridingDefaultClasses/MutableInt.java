package org.sheet_midusic.stuff.OverridingDefaultClasses;

import java.util.concurrent.atomic.AtomicInteger;

public class MutableInt
{
	private int value;

	public MutableInt(int value) {
		this.value = value;
	}

	public int incr(int n) { return (this.value += n) - n; }

	public int incr() { return value++; }
}
