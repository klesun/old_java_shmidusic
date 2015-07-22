package stuff.tools.jmusic_integration.JmModel;

import blockspace.staff.Staff;
import blockspace.staff.accord.Accord;
import blockspace.staff.accord.nota.Nota;
import jm.music.data.Part;
import stuff.tools.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// A Part is container of Phrase-s played in common channel
public class JmPartMaker {

	public static List<Part> makeListFrom(Staff staff) {

		Set<Integer> channels = new HashSet<>();
		for (Accord a: staff.getAccordList()) {
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
