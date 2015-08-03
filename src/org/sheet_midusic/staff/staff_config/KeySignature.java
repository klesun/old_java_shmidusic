package org.sheet_midusic.staff.staff_config;

import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;

import java.util.*;

public class KeySignature
{
	final public int signature;

	final static int DO = 0, RE = 1, MI = 2, FA = 3, SO = 4, LA = 5, TI = 6;

	final static List<Set<Integer>> sharpSignatures;
	final static List<Set<Integer>> flatSignatures;

	public KeySignature(int signature)
	{
		this.signature = signature;
	}

	public int calcIvoryMask(int tune) {
		int tuneMask = tune % 12;
		if (matches(tuneMask)) {
			return myTuneQueue().indexOf(tuneMask);
		} else {
			return flatIvoryMask(tune); // for now we will draw all unknown Nota-s as bemol
		}
	}

	private Boolean matches(int tuneMask) {
		return myTuneQueue().contains(tuneMask);
	}

	public List<Integer> myTuneQueue()
	{
		List<Integer> result = new LinkedList<>();
		for (int i = 0; i <= TI; ++i) {
			int tune = defaultTuneQueue().get(i);
			if (signature > 0 && sharpSignatures.get(signature - 1).contains(i)) {
				++tune;
			} else if (signature < 0 && flatSignatures.get(Math.abs(signature) - 1).contains(i)) {
				--tune;
			}
			result.add(tune);
		}

		return result;
	}

	private static List<Integer> defaultTuneQueue() {
		return Arrays.asList(0,2,4, 5,7,9,11);
	}

	// treating all ebonies as flats
	static int flatIvoryMask(int tune) {
		return Arrays.asList(0,1,1,2,2,3,4,4,5,5,6,6).get(tune % 12);
	}

	public Set<Integer> getAffectedIvorySet()
	{
		Set<Integer> result = new HashSet<>();

		if (signature > 0) {
			result.addAll(sharpSignatures.get(signature - 1));
		} else if (signature < 0) {
			result.addAll(flatSignatures.get(Math.abs(signature) - 1));
		}

		return result;
	}

	static {
		sharpSignatures = Arrays.asList(
			set(FA),
			set(FA, DO),
			set(FA, DO, SO),
			set(FA, DO, SO, RE),
			set(FA, DO, SO, RE, LA),
			set(FA, DO, SO, RE, LA, MI),
			set(FA, DO, SO, RE, LA, MI, TI)
		);

		flatSignatures = Arrays.asList(
			set(TI),
			set(TI, MI),
			set(TI, MI, LA),
			set(TI, MI, LA, RE),
			set(TI, MI, LA, RE, SO),
			set(TI, MI, LA, RE, SO, DO),
			set(TI, MI, LA, RE, SO, DO, FA)
		);

		Map<Integer, List<Integer>> ebonySignMap = new TruMap<>()
			.p(-7, Arrays.asList(-1, -1, -1, -1, -1, -1, -1))
			.p(-6, Arrays.asList(-1,-1,-1, 0,-1,-1,-1))
			.p(-5, Arrays.asList( 0,-1,-1, 0,-1,-1,-1))
			.p(-4, Arrays.asList( 0,-1,-1, 0, 0,-1,-1))
			.p(-3, Arrays.asList( 0, 0,-1, 0, 0,-1,-1))
			.p(-2, Arrays.asList( 0, 0,-1, 0, 0, 0,-1))
			.p(-1, Arrays.asList( 0, 0, 0, 0, 0, 0,-1))
			.p( 0, Arrays.asList( 0, 0, 0, 0, 0, 0, 0))
			.p(+1, Arrays.asList( 0, 0, 0,+1, 0, 0, 0))
			.p(+2, Arrays.asList(+1, 0, 0,+1, 0, 0, 0))
			.p(+3, Arrays.asList(+1, 0, 0,+1,+1, 0, 0))
			.p(+4, Arrays.asList(+1,+1, 0,+1,+1, 0, 0))
			.p(+5, Arrays.asList(+1,+1, 0,+1,+1,+1, 0))
			.p(+6, Arrays.asList(+1,+1,+1,+1,+1,+1, 0))
			.p(+7, Arrays.asList(+1,+1,+1,+1,+1,+1,+1))
			;
	}

	public static <T> Set<T> set(T... args)
	{
		Set<T> result = new HashSet<>();
		for (T arg: args) {
			result.add(arg);
		}
		return result;
	}
}
