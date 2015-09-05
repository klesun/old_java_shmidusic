package org.shmidusic.stuff.screwed;

// was about to implement own SMF, but after few days of investigating
// i realised that opening a binary midi file is ridiculously complicated for
// a simple guy like me. it would take a huge piece of time to invent and debug
// i you wanna use my Explain-based approach and if you don't, there is no need to
// reinvent a quadrocycle when somebody already wasted their nerves on this problem

import org.klesun_model.Explain;

import java.util.ArrayList;
import java.util.List;

/** @screwed */
public class MidiFileData
{
	public int beetsPerSecond;

    int fileType = -100;
    int trackCount = -100;
    int unitsPerSecond = -100;

    private static Explain<Long> lex(Integer l) {
        return new Explain<>(new Long(l));
    }

    private static Explain<Long> lex(Long l) {
        return new Explain<>(new Long(l));
    }

	public static Explain<MidiFileData> constructFrom(byte[] bytes)
    {
        // TODO: maybe make special class "MidiByteReading" and move logic of this method there

//        MutableInt atIndex = new MutableInt(0);
//        Function<Integer, Integer> read = n -> IntStream.range(0, n)
//                .map(k -> bytes[atIndex.incr()] << ((n - k - 1) * 8)) // remember, big is first like in decimals
//                .reduce(Math::addExact).orElse(0);
//
//        Function<Integer, Explain<Integer>> readSafe = n -> new Explain(n + atIndex.get() < bytes.length, "Unexpected EOF").ifSuccess(read);
//        Supplier<Explain<Integer>> current = () -> new Explain(atIndex.get() < bytes.length, "EOF").andThen(() -> new Explain(bytes[atIndex.get()]));
//
//        // i'm very unsure about correctness of this
//        Supplier<Explain<Long>> readVariableLength = () -> {
//            return lex(0).whileIf(
//                    () -> current.get().andIf(i -> (i & 0x80) == 0, "end of var").isSuccess(), // peace on Earth
//                    sum -> readSafe.apply(1).ifSuccess(b -> lex((sum << 7) + (b & 0x7F))) // vvvv wwww  wwwz zzzz  zzyy yyyy  yxxx xxxx | poor people of the past... poor me
//                );
//        };
//
//        Supplier<Explain<Track>> readTrack = () -> readSafe.apply(4).andIf(i -> i != 0x4D54726B, "MTrk (track's first bytes) are wrong")
//                .andThen(() -> readSafe.apply(4)) // If MTrk read ok get bytesRemaining
//                .andThen(() -> new Explain<>(new Track()))
//                .ifSuccess(t -> readVariableLength.get().ifSuccess(num -> new Explain<>(t.setDeltaTime(num))))
//                .
//                ;
//
//        Function<MidiFileData, Explain<MidiFileData>> readTracks = subj -> {
//
//            return IntStream.range(0, subj.trackCount).boxed()
//                    .map(trackNum -> readTrack.get().whenSuccess(t -> subj.trackList.add(t)))
//                    .reduce((a, b) -> b.andThen(() -> a))
//                    .orElse(new Explain<>(false, "Empty track list"))
//                    .andThen(() -> new Explain<>(subj))
//                    ;
//        };
//
//        BiFunction<Integer, Function<Integer, MidiFileData>, Explain<MidiFileData>> readSet = (n, setter) ->
//                readSafe.apply(n).ifSuccess(num -> new Explain<>(setter.apply(num)));
//
//
//        // see http://www.music.mcgill.ca/~ich/classes/mumt306/SMFformat.html for midi header format
//        return readSafe.apply(4).andIf(i -> i != 0x4D546864, "This is not midi file!")
//                .andThen(() -> new Explain<>(new MidiFileData()))
//                .ifSuccess(self -> readSet.apply(2, self::setFileType))
//                .ifSuccess(self -> readSet.apply(2, self::setTrackCount))
//                .ifSuccess(self -> readSet.apply(2, self::setUnitsPerSecond))
//                .ifSuccess(self -> readTracks.apply(self))
//                ;
        return new Explain<MidiFileData>(false, "screw this, will use ready solutions");
    }

    private MidiFileData() {}

    private void addEvent(byte data1, byte data2, byte data3) {
        // TODO: do something
    }

    // TODO: check, i'm interested, what's this
    private MidiFileData setFileType(int value) {
        this.fileType = value;
        return this;
    }

    private MidiFileData setTrackCount(int value) {
        this.trackCount = value;
        return this;
    }

    private MidiFileData setUnitsPerSecond(int value) {
        this.unitsPerSecond = value;
        return this;
    }

	private class NoteEvent
	{
		final int tune;
		final long startOn;
		final int duration;

		public NoteEvent(int tune, int startOn, int duration) {
			this.tune = tune;
			this.startOn = startOn;
			this.duration = duration;
		}
	}
}
