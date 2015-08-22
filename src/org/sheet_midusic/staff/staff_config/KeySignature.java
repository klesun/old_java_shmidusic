package org.sheet_midusic.staff.staff_config;

import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;
import org.sheet_midusic.stuff.tools.jmusic_integration.INota;

import java.util.*;
import java.util.function.BiConsumer;

public class KeySignature
{
	// TODO: it's very open question, how to determine whether it's flat or sharp (or straight)
	// when a Note that is not present in Staff's main key signature is pressed
	// i prophet a musician would know the answer. Check how it's done in Musescore, maybe?

	// for now we treat all not predicted ebonies as flats and ivories as becars, but i may hear
	// that they are not sometimes. we may get some issue like: [si-bemol; si-becar; si-bemol] when
	// it obviously is [la-diez; si; la]

	// TODO: i think it's not needed
	final private int signature;

	final static int DO = 0, RE = 1, MI = 2, FA = 3, SO = 4, LA = 5, TI = 6;

	final static List<Set<Integer>> sharpSignatures;
	final static List<Set<Integer>> flatSignatures;

	final Set<Integer> mySharpSignature = new HashSet<>();
	final Set<Integer> myFlatSignature = new HashSet<>();

	public KeySignature(int signature)
	{
		this.signature = signature;

		if (signature > 0) {
			mySharpSignature.addAll(sharpSignatures.get(signature - 1));
		} else if (signature < 0) {
			myFlatSignature.addAll(flatSignatures.get(Math.abs(signature) - 1));
		}
	}

	// что делает этот метод???
	public int calcIvoryMask(int tune) {
		int tuneMask = tune % 12;
		if (matches(tuneMask)) {
			return myTuneQueue().indexOf(tuneMask);
		} else {
			return flatIvoryMask(tune); // for now we will draw all unknown Nota-s as bemol
		}
	}

	// add bemols/becars/diezes of this chord Note-s
	public void consume(Chord chord)
	{
		chord.notaStream().forEach(n -> {
			if (!myTuneQueue().contains(n.getTune() % 12)) {
				if (n.isEbony()) {
					// treating as bemol
					// TODO: maybe taking same symbol as signature direction would be better (see eflen lied)
					myFlatSignature.add(flatIvoryMask(n.getTune()));
				} else {
					// becar
					mySharpSignature.remove(sharpIvoryMask(n.getTune()));
					myFlatSignature.remove(flatIvoryMask(n.getTune()));
				}
			}
		});
	}

	private Boolean matches(int tuneMask) {
		return myTuneQueue().contains(tuneMask);
	}

	public List<Integer> myTuneQueue()
	{
		List<Integer> result = new LinkedList<>();
		for (int i = 0; i <= TI; ++i) {
			int tune = defaultTuneQueue().get(i);
			if (mySharpSignature.contains(i)) {
				++tune;
			}
			if (myFlatSignature.contains(i)) {
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

	static int sharpIvoryMask(int tune) {
		return Arrays.asList(0,0,1,1,2,3,3,4,4,5,5,6).get(tune % 12);
	}

	public Set<Integer> getAffectedIvorySet()
	{
		Set<Integer> result = new HashSet<>();
		result.addAll(mySharpSignature);
		result.addAll(myFlatSignature);

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
