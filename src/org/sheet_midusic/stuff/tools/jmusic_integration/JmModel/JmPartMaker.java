package org.sheet_midusic.stuff.tools.jmusic_integration.JmModel;

import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.chord.Chord;
import org.jm.music.data.Part;

import java.util.*;
import java.util.stream.Collectors;

// A Part is container of Phrase-s played in common channel
public class JmPartMaker {

	public static List<Part> makeListFrom(Staff staff) {

		Set<Integer> channels = new HashSet<>();
		for (Chord a: staff.getChordList()) {
			channels.addAll(a.getNotaSet().stream().map(n -> n.getChannel()).collect(Collectors.toSet()));
		}

		return channels.stream()
				.map(channel -> makeFrom(staff, channel))
				.collect(Collectors.toList());
	}

	private static Part makeFrom(Staff staff, int channel) {

		Part part = new Part();

		part.setChannel(channel);
		part.setInstrument(staff.getConfig().channelList.get(channel).getInstrument());
		part.addPhraseList(JmPhrase.makeListFrom(staff, channel));

		return part;
	}
}
