package org.shmidusic.stuff.scripts;

// this script takes midi file and outputs json representation of this file
// SMF class reading result

import org.shmidusic.stuff.OverridingDefaultClasses.MutableInt;
import org.shmidusic.stuff.midi.NoteGuesser;
import org.shmidusic.stuff.midi.standard_midi_file.SMF;
import org.shmidusic.stuff.midi.standard_midi_file.Track;
import org.shmidusic.stuff.midi.standard_midi_file.event.Event;
import org.json.JSONArray;
import org.json.JSONObject;
import org.klesun_model.Explain;
import org.shmidusic.stuff.tools.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MidiToReadableMidi
{
    public static void main(String[] args) throws IOException
    {
		List<String> dirNames = Arrays.asList(".", "watched", "random_good_stuff");
		String base = "/home/klesun/Dropbox/midiCollection/";

        MutableInt counter = new MutableInt(0);

		dirNames.stream()
		.map(d -> base + d).map(File::new).flatMap(f -> Arrays.stream(f.listFiles()))
		.filter(f -> f.getPath().endsWith(".mid")).forEach(source -> {

			/** @debug */
			System.out.println(counter.incr() + " Processing " + source);

			File destination = new File("/home/klesun/Dropbox/midiCollection_smf/" + source.getName() + ".js"); // hoping that file names does not repeat...

			try {
				SMF smf = new SMF();
				smf.read(new FileInputStream(source));
                JSONObject midJs = new NoteGuesser(smf).generateMidiJson();
				writeTextToFile(midJs.toString(2), destination);
			} catch (Exception exc) {
				Logger.fatal(exc, "Unexpected exception while processing " + source.getName());
			}
		});
    }

    private static Explain writeTextToFile(String text, File f) throws IOException
    {
        PrintWriter out = new PrintWriter(f);
        out.println(text);
        out.close();

        return new Explain(true);
    }
}
