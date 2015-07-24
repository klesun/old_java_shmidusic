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
import stuff.tools.jmusic_integration.INota;
import stuff.tools.jmusic_integration.JmNote;

import java.util.*;
import java.util.function.Function;

// this class imitates MidiParser in jm package, but simpler and for midiana model
// cuz i realized it would be pain in the ass to reuse their, Phrase-s was bad designer decision imho
public class SimpleMidiParser
{
	// imitates MidiParser::scoreToSMF()
	public static SMF staffToSmf(Staff staff)
	{
		// tempo 120 - semibreve - 960 miliseconds - qppt - 480
		// tempo 90 - semibreve
		SMF smf = new SMF((short)1, (short)(staff.getConfig().getTempo() * 4)); // 1 i think means nothing, and tempo should be 480 by convention probably
		// TODO: for non-120 tempo breaks

		// setting tempo. i suspect TimeSig and KeySig have absolute no influence on how things sound
		// just facultative information to ease parse for whoever gets this midi file in future

		Function<Fraction, Integer> time = f -> Nota.getTimeMilliseconds(f, staff.getConfig().getTempo());
		Function<Channel, Event> mapChannel = c -> new PChange(c.getInstrument().shortValue(), c.channelNumber.get().shortValue(), 0);

		smf.getTrackList().add(new Track()
//				.addEvent(new TempoEvent(0, staff.getConfig().getTempo())) // TODO: xml tells me we put 500000 here for some reason, maybe nahuj?
				.addEvent(new TimeSig(0, staff.getConfig().getNumerator(), Staff.DEFAULT_ZNAM))
				.addEvents(staff.getConfig().channelList.get().stream().map(mapChannel))
//				.addEvent(new KeySig(0, nevhujebu)) // shadow magic of educated musicians, try http://www.recordingblogs.com/sa/tabid/88/Default.aspx?topic=MIDI+Key+Signature+meta+message
				.addEvent(new EndTrack())
		);

		Map<Integer, Track> trackDict = new HashMap<>();

		/** @debug - uncomment when done */
		Fraction now = new Fraction(0); // supposing we have only good fractions, no 12759382/4832012124 and alike
		for (Accord accord: staff.getAccordList()) {

			for (Nota nota: accord.getNotaSet()) {

				if (!trackDict.containsKey(nota.getChannel())) {
					trackDict.put(nota.getChannel(), new Track());
				}

				// TODO: handle our hack with drums (they are not general Nota-s even though they are stored like that in 10 channel)

				// TODO: abstractionize this process and merge it with Playback process, so stuff like drums would not appear

				trackDict.get(nota.getChannel()).addEvent(new NoteOn(nota, time.apply(now)));
				trackDict.get(nota.getChannel()).addEvent(new NoteOff(nota, time.apply(now.add(nota.getRealLength()))));
			}

			/** @debug */
			System.out.println("zhopa " + now + " " + time.apply(now));

			now = now.add(accord.getFraction());
		}

		trackDict.values().forEach(t ->
		{
			convertAbsoluteTimesToRelative(t);
			t.addEvent(new EndTrack());
			smf.getTrackList().add(t);
		});

		return smf;
	}

	/** @param track - track with absolute event timing */
	private static void convertAbsoluteTimesToRelative(Track track)
	{
		Collections.sort(track.getEvtList(), cmp(Event::getTime));

		int time = 0; // milliseconds
		for (Event event: track.getEvtList()) { // god bless the incompetent programmer for not making them immutable!
			int tmpTime = event.getTime();
			event.setTime(event.getTime() - time);
			time = tmpTime;
		}
	}

	private static <T> Comparator<T> cmp(Function<T, Integer> getOrder) {
		return (a, b) -> getOrder.apply(a) - getOrder.apply(b);
	}
}
