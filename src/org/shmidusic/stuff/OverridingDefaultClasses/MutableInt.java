package org.shmidusic.stuff.OverridingDefaultClasses;

public class MutableInt
{
	private int value;

	public MutableInt(int value) {
		this.value = value;
	}

	public int incr(int n) { return (this.value += n) - n; }

	public int incr() { return value++; }

    public int decr() { return value--; }

    public int get() {
        return value;
    }
}
