package org.shmidusic.stuff.midi;

import org.klesun_model.Explain;
import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.staff_config.Channel;
import org.shmidusic.stuff.midi.standard_midi_file.SMF;
import org.shmidusic.stuff.midi.standard_midi_file.Track;
import org.shmidusic.stuff.midi.standard_midi_file.event.*;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.stuff.musica.Playback;
import org.shmidusic.stuff.screwed.MidiFileData;
import org.shmidusic.stuff.tools.INota;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// this class imitates MidiParser in org.jm package, but simpler and for midiana model
// cuz i realized it would be pain in the ass to reuse their, Phrase-s was bad designer decision imho
public class SimpleMidiParser
{
	public static Explain<MidiFileData> readMidiFromBytes(byte[] bytes)
	{
		return MidiFileData.constructFrom(bytes);
	}

	// imitates MidiParser::scoreToSMF()
	public static SMF sheetMusicToSmf(SheetMusic sheetMusic)
	{
		// TODO: take into account multiple Staff-s!
		Staff staff = sheetMusic.staffList.get(0);

		// 480 - for tempo 120
		// 960 - for tempo 60 - taking it for base cuz it's really close to 1000: 960 beats are 1000 miliseconds
		SMF smf = new SMF((short)1, (short)(60 * 960 / staff.getConfig().getTempo())); // 1 i think means nothing, and tempo should be 480 by convention probably

		Function<Channel, Event> mapChannelInstrument = c -> new PChange(c.getInstrument().shortValue(), c.channelNumber.get().shortValue(), 0);
		Function<Channel, Event> mapChannelVolume = c -> new CChange(DeviceEbun.CONTROL_CHANGE_VOLUME, c.getVolume().shortValue(), c.channelNumber.get().shortValue(), 0);

		Set<Integer> usedChannels = staff.chordStream()
							.map(c -> c.notaStream().map(INota::getChannel))
							.flatMap(s -> s)
							.distinct()
							.collect(Collectors.toSet());

		List<Channel> channels = staff.getConfig().channelList.get().stream()
							.filter(c -> usedChannels.contains(c.channelNumber.get()))
							.collect(Collectors.toList());

		smf.getTrackList().add(new Track()
				.addEvent(new TempoEvent(0, staff.getConfig().getTempo()))
				.addEvent(new TimeSig(0, staff.getConfig().getTactSize().getNumerator(), staff.getConfig().getTactSize().getDenominator()))
				.addEvents(channels.stream().map(mapChannelInstrument))
				.addEvents(channels.stream().map(mapChannelVolume))
				.addEvent(new KeySig(Math.abs(staff.getConfig().keySignature.get()), (int) Math.signum(staff.getConfig().keySignature.get())))
				.addEvent(new EndTrack())
		);

		Map<Integer, Track> trackDict = new HashMap<>();

		new Playback(new StaffComponent(staff, null) /* XD */).streamTo(new SmfScheduler(trackDict, staff.getConfig()));

		// TODO: handle our hack with drums (they are not general Nota-s even though they are stored like that with 10 channel)


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
