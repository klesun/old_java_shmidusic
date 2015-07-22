package stuff.Midi;

import blockspace.staff.Staff;
import blockspace.staff.StaffConfig.Channel;
import blockspace.staff.accord.Accord;
import blockspace.staff.accord.nota.Nota;
import jm.midi.SMF;
import jm.midi.Track;
import jm.midi.event.*;
import org.apache.commons.math3.fraction.Fraction;
import stuff.tools.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// this class imitates MidiParser in jm package, but simpler and for midiana model
// cuz i realized it would be pain in the ass to reuse their, Phrase-s was bad designer decision imho
public class SimpleMidiParser
{
	// imitates MidiParser::scoreToSMF()
	public static SMF staffToSmf(Staff staff)
	{
		SMF smf = new SMF();

		// setting tempo. i suspect TimeSig and KeySig have absolute no influence on how things sound
		// just facultative information to ease parse for whoever gets this midi file in future

		Function<Fraction, Integer> time = f -> Nota.getTimeMilliseconds(f, staff.getConfig().getTempo());
		Function<Channel, Event> mapChannel = c -> new PChange(c.getInstrument().shortValue(), c.channelNumber.get().shortValue(), 0);

		smf.getTrackList().add(new Track()
				.addEvent(new TempoEvent(0, staff.getConfig().getTempo()))
				.addEvent(new TimeSig(0, staff.getConfig().getNumerator(), Staff.DEFAULT_ZNAM))
				.addEvents(staff.getConfig().channelList.get().stream().map(mapChannel))
//				.addEvent(new KeySig(0, nevhujebu)) // shadow magic of educated musicians, try http://www.recordingblogs.com/sa/tabid/88/Default.aspx?topic=MIDI+Key+Signature+meta+message
				.addEvent(new EndTrack())
		);

		Map<Integer, Track> trackDict = new HashMap<>();

		Fraction now = new Fraction(0); // supposing we have only good fractions, no 12759382/4832012124 and alike
		for (Accord accord: staff.getAccordList()) {

			for (Nota nota: accord.getNotaSet()) {

				if (!trackDict.containsKey(nota.getChannel())) {
					trackDict.put(nota.getChannel(), new Track());
				}

				trackDict.get(nota.getChannel()).addEvent(new NoteOn(nota, time.apply(now)));
				trackDict.get(nota.getChannel()).addEvent(new NoteOff(nota, time.apply(now.add(nota.getRealLength()))));
			}

			/** @debug */
			System.out.println("now: " + now);
			System.out.println("time: " + time.apply(now));
			System.out.println("off on: " + time.apply(now.add(accord.getNotaSet().first().getRealLength())));
			System.out.println();

			now = now.add(accord.getFraction());
		}

		// TODO: где noteOff БЛЕАТЬ! похоже то что я передаю это всё-таки разница, а не абсолютное время...

		trackDict.values().forEach(t -> t.addEvent(new EndTrack()));

		trackDict.values().forEach(smf.getTrackList()::add);

		return smf;
	}

}
