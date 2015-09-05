package org.shmidusic.stuff.scripts;

// this script takes midi file and outputs json representation of this file
// SMF class reading result

import org.shmidusic.stuff.midi.standard_midi_file.SMF;
import org.shmidusic.stuff.midi.standard_midi_file.Track;
import org.shmidusic.stuff.midi.standard_midi_file.event.Event;
import org.json.JSONArray;
import org.json.JSONObject;
import org.klesun_model.Explain;

import java.io.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MidiToReadableMidi
{
    public static void main(String[] args) throws IOException
    {
        File source = new File("Z:/Dropbox/midiCollection/random_good_stuff/morrowind_main.mid");
        File destination = new File("Z:/Dropbox/midiCollection/random_good_stuff/morrowind_main.mid.js");

        SMF smf = new SMF();
        smf.read(new FileInputStream(source));

        JSONObject result = new JSONObject();
        result.put("division", smf.getPPQN());
        JSONArray trackList = new JSONArray();

        BiFunction<Integer, String, String> space = (width, text) ->
                (width > text.length())
                    ? new String(new char[width - text.length()]).replace("\0", " ") + text
                    : text.substring(0, width);

        for (Track track: smf.getTrackList()) {
            JSONArray eventList = new JSONArray();
            for (Event event: track.getEvtList()) {

                ByteArrayOutputStream bos = new ByteArrayOutputStream(4);
                DataOutputStream os = new DataOutputStream(bos);
                event.write(os);
                byte[] byteArray = bos.toByteArray();
                String bytes = IntStream.range(0, byteArray.length).map(n -> byteArray[n] & 0xFF).boxed()
                        .map(n -> space.apply(3, n + ""))
                        .collect(Collectors.joining(", "));

                eventList.put(new JSONObject()
                        .put("eventName", space.apply(10, event.getClass().getSimpleName()))
                        .put("deltaTime", space.apply(5, "" + event.getTime()))
                        .put("bytes", bytes)
                );
            }

            trackList.put(new JSONObject().put("eventList", eventList));
        }

        result.put("trackList", trackList);

        writeTextToFile(result.toString(2), destination);
    }

    private static Explain writeTextToFile(String text, File f) throws IOException
    {
        PrintWriter out = new PrintWriter(f);
        out.println(text);
        out.close();

        return new Explain(true);
    }
}
