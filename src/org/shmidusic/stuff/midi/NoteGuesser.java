package org.shmidusic.stuff.midi;

// this class transforms midi event list to sheet music
// obvious from name, it guesses note lengths from event timestamp and unitsPerSecond

import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.Main;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.sheet_music.SheetMusicComponent;
import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.chord.nota.Nota;
import org.shmidusic.sheet_music.staff.staff_config.StaffConfig;
import org.shmidusic.stuff.midi.standard_midi_file.SMF;
import org.shmidusic.stuff.midi.standard_midi_file.Track;
import org.shmidusic.stuff.midi.standard_midi_file.event.*;
import org.shmidusic.stuff.tools.INota;
import org.shmidusic.stuff.tools.Logger;

import javax.swing.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** this class generates shmidusic SheetMusic from Standard Midi File data */
public class NoteGuesser
{
    /** conventional unit count in one second.
      * all midi events have conventional units timestamp, cuz 1000 milliseconds does not divide to 3. (why ain't we babylonians?)
      * I prefer to use 960 as unitsPerSecond, cuz it' really close to 1000 ms */
	final int unitsPerSecond;
    int tempo = 120; // we'll get tempo from some events. pray to your god that it will be in the first track exactly at the 0th second

    final SMF midiFile;

	public NoteGuesser(SMF smf) {
		this.midiFile = smf;
        this.unitsPerSecond = smf.getPPQN();
	}

    public SheetMusic generateSheetMusic(Consumer<SheetMusic> rebuild)
    {
        SheetMusic sheetMusic = new SheetMusic();
        Staff staff = sheetMusic.staffList.get(0);

        List<Note> notes = new ArrayList<>();

        for (Track track: midiFile.getTrackList()) {

            List<Note> opened = new ArrayList<>();
            int time = 0;
            for (Event event : track.getEvtList()) {

                time += event.getTime();
                int finalTime = time;

                if (event instanceof NoteOn && ((NoteOn)event).getVelocity() > 0) {

                    NoteOn noteOn = (NoteOn)event;
                    opened.add(new Note(noteOn.getPitch(), noteOn.getMidiChannel(), time));

                } else if (event instanceof NoteOff || (event instanceof NoteOn && ((NoteOn)event).getVelocity() == 0)) {

                    INoteEvent noteOff = (INoteEvent)event;
                    opened.stream().filter(n -> n.tune == noteOff.getPitch() && n.channel == noteOff.getMidiChannel())
                    .findAny().ifPresent(n ->
                    {
                        n.setDuration(finalTime - n.time);
                        notes.add(n);
                        opened.remove(n);
                    });

                } else {
                    // handle staff config event
                    if (time == 0) {
                        consumeConfigEvent(event, staff.getConfig());
                    } else {
                        Logger.warning("Config event not at the start of midi: " + event.getClass().getSimpleName() + " " + event.getTime() + ". Should we split Staff in future on this case?");
                    }
                }
            }
        }

        notes.stream().sorted((n1,n2) -> n1.time - n2.time).forEach(n -> {
            /** @debug */
            System.out.println(guessPos(n.time) + " " + n.strMe());
            putAt(guessPos(n.time), n, staff);

            /** @debug */
//            rebuild.accept(sheetMusic);
//            JOptionPane.showMessageDialog(Main.window, "zhopa");
        });

        return sheetMusic;
    }

    private void consumeConfigEvent(Event event, StaffConfig config)
    {
        if (event instanceof TempoEvent) {
            tempo = (int)((TempoEvent) event).getTempo();
//            config.setTempo(tempo);
        } else if (event instanceof PChange) {
            PChange instrument = (PChange) event;
            config.channelList.get(instrument.getMidiChannel()).setInstrument(instrument.getValue());
        } else {
            // ...
        }
    }


    /** @return - nota that we just put */
    private static Nota putAt(Fraction desiredPos, INota nota, Staff staff)
    {
        // TODO: it's broken somehow. Bakemonogatari can be opent with Noteworthy, but cant with midiana

        Fraction curPos = new Fraction(0);
        for (int i = 0; i < staff.getChordList().size(); ++i) {

            if (curPos.equals(desiredPos)) {
                Chord chord = staff.getChordList().get(i);

                Fraction wasAccordLength = chord.getFraction();
                Nota newNota = chord.addNewNota(nota);
                chord.removeRedundantPauseIfAny();

                if (!wasAccordLength.equals(chord.getFraction())) {
                    // putting filler in case when chord length became smaller to preserve timing
                    Fraction dl = wasAccordLength.subtract(chord.getFraction());
                    staff.addNewAccord(i + 1).addNewNota(0, 0).setLength(dl);
                }
                return newNota;
            } else if (curPos.compareTo(desiredPos) > 0) {

                Chord chord = staff.getChordList().get(i - 1);
                Fraction offset = new Fraction(curPos.doubleValue() - desiredPos.doubleValue());
                Fraction onset = new Fraction(chord.getFraction().doubleValue() - offset.doubleValue());

                chord.addNewNota(0, 0).setLength(onset);

                Chord newChord = staff.addNewAccord(i);
                Nota newNota = newChord.addNewNota(nota);
                if (newNota.getLength().compareTo(offset) > 0) {
                    // TODO: maybe if last chord in staff then no need
                    // put nota with onset length into newNota's chord to preserve timing
                    newChord.addNewNota(0, 0).setLength(offset);
                } else if (newNota.getLength().compareTo(offset) < 0) {
                    // TODO: maybe if last chord in staff then no need
                    // put an empty nota after and set it's length(onset - newNota.getLength())
                    staff.addNewAccord(i + 1).addNewNota(0, 0).setLength(offset.subtract(newNota.getLength()));
                }

                return newNota;
            }

            Fraction accordFraction = staff.getChordList().get(i).getFraction();

            curPos = new Fraction(curPos.doubleValue() + accordFraction.doubleValue());
        }

        Fraction rest = desiredPos.subtract(curPos);
        if (!rest.equals(new Fraction(0))) {
            staff.addNewAccord().addNewNota(0,0).setLength(rest);
        }

        // TODO: we don't handle here pause prefix! (i.e when desired start is more than end) !!!
        // if not returned already
        return staff.addNewAccord().addNewNota(nota);
    }

    private Fraction guessPos(int unitsArg)
    {
        Cont<Integer> unitsLeft = new Cont<>(unitsArg);
        Cont<Fraction> result = new Cont<>(fr(0, 1));

        while (unitsLeft.get() > toUnits(fr(2,1))) {
            unitsLeft.set(unitsLeft.get() - toUnits(fr(2,1)));
            result.set(result.get().add(fr(2, 1)));
        }

        getPossibleLengths()
        .stream()
        .sorted()
        .collect(Collectors.toCollection(LinkedList::new))
        .descendingIterator().forEachRemaining(length ->
        {
            if (toUnits(length) <= unitsLeft.get()) {
                result.set(result.get().add(length));
                unitsLeft.set(unitsLeft.get() - toUnits(length));
            }
        });

        return result.get();
    }

    private Fraction guessLength(int units)
    {
        List<Fraction> lengths = getPossibleLengths();

        // plus handle possibility that it is two linked Note-s... or may be even no...
        int error = lengths.stream().map(f -> Math.abs(units - toUnits(f))).sorted().findFirst().get();
        return lengths.stream().filter(f -> Math.abs(units - toUnits(f)) == error).findAny().get();
    }

    private static List<Fraction> getPossibleLengths()
    {
        List<Fraction> lengths = asList(fr(2, 1));
        // all accepted variations of semibreve: clean | triplet| with dot | with two dots
        List<Fraction> semibreves = asList(fr(1, 1), fr(1, 3), fr(3, 2), fr(7, 4));
        lengths.addAll(semibreves);
        // half
        lengths.addAll(asList(fr(1, 2), fr(1, 6), fr(3, 4), fr(7, 8)));
        // quarter
        lengths.addAll(asList(fr(1, 2), fr(1, 12), fr(3, 8), fr(7, 16)));
        // 1/8 does not have triplet and two dots
        lengths.addAll(asList(fr(1, 8), fr(3, 16)));
        // 1/16 does not need triplet and dots
        lengths.addAll(asList(fr(1,16)));

        return lengths;
    }

    public static <T> ArrayList<T> asList(T... a) {
        ArrayList<T> list = new ArrayList<>();
        for (T elem: a) {
            list.add(elem);
        }
        return list;
    }

    private int toUnits(Fraction length) {
        return length.multiply(unitsPerSecond).intValue();
    }

    private static Fraction fr(int num, int den) {
        return new Fraction(num, den);
    }

    private class Note implements INota
    {
        final int time;
        final int tune;
        final int channel;
        int duration = -100;

        public Note(int tune, int channel, int time) {
            this.time = time;
            this.tune = tune;
            this.channel = channel;
        }

        public Note setDuration(int value) {
            this.duration = value;
            return this;
        }

        public Integer getTune() { return tune; }
        public Integer getChannel() { return channel; }
        public Fraction getLength() {
            return isTriplet()
                    ? getActualLength().multiply(3)
                    : getActualLength();
        }
        public Boolean isTriplet() {
            return getActualLength().getDenominator() % 3 == 0;
        }

        private Fraction getActualLength() {
            return guessLength(this.duration);
        }

        public String strMe() {
            return "time: " + time + "; tune: " + tune + "; channel: " + channel + "; duration: " + duration + "; length: " + getActualLength();
        }
    }

    private class Cont<T>
    {
        private T value;

        public Cont(T value) {
            this.value = value;
        }

        public Cont<T> set(T value) {
            this.value = value;
            return this;
        }

        public T get() {
            return value;
        }
    }
}
