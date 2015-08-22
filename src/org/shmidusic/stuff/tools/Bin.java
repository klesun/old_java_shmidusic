package org.shmidusic.stuff.tools;

public class Bin {

	public static Boolean isPowerOf2(int n) {
		return (n & (n - 1)) == 0;
	}

}
