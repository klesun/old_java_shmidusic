package org.shmidusic.stuff.tools.jmusic_integration.JmModel;


import org.shmidusic.staff.Staff;
import org.shmidusic.staff.chord.Chord;
import org.shmidusic.staff.chord.nota.Nota;
import org.jm.music.data.Phrase;
import org.shmidusic.stuff.tools.Logger;
import org.shmidusic.stuff.tools.jmusic_integration.INota;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// A Phrase is sequence of Nota-s played in one channel. only one Nota at a time
public class JmPhrase {

	private List<INota> noteList = new ArrayList<>();

	public static List<Phrase> makeListFrom(Staff staff, int channel) {

		Predicate<Nota> filter = n -> n.getChannel() == channel;
		Function<Chord, Integer> cnt = a -> (int)a.notaStream(filter).count();

		// fuck performance. long live readability! // long live readability блеать
		long phraseCount = cnt.apply(staff.getChordList().stream().max(cmp(cnt)).get());

		List<Phrase> result = new ArrayList<>();
		for (int i = 0; i < phraseCount; ++i) {
			result.add(makeFrom(staff, channel, i));
		}

		return result;
	}

	// TODO: instead of org.shmidusic.staff and channel and notaOrder better pass here already filtered sequence with pauses on place of emptyness
	private static Phrase makeFrom(Staff staff, int channel, int notaOrder)
	{
		Phrase phrase = new Phrase();

		for (Chord chord : staff.getChordList()) {
			Stream<Nota> notas = chord.notaStream(n -> n.getChannel() == channel);
			if (notas.count() < notaOrder) {
				// добавить ноту
//				if (/*длина ноты больше сука я не знаю что нам делать нахуй их, сам распаршу*/) {
//
//				}
			} else {
				// добавить паузу
			}
		}

		Logger.fatal("Not Implemented Yet");

		return phrase;
	}

	public static List<JmPhrase> getPhraseList(Staff staff) {

		List<JmPhrase> result = new ArrayList<>();

//		for (int channel = 0; channel < Channel.CHANNEL_COUNT; ++channel) {
//
//			final int channelFinal = channel;
//
//			// TODO: add exact number of Phrases on this step, as needed, using
//			int channlePhraseCount = org.shmidusic.staff.getChordList().stream()
//					.max((a, b) -> a.getNotaSet().size() - b.getNotaSet().size())
//					.get().getNotaSet().size();
//			List<JmPhrase> channelPhrases = new ArrayList<>();
//			for (int i = 0; i < channlePhraseCount; ++i) {
//				channelPhrases.add(new JmPhrase());
//			}
//
//			for (Chord chord: org.shmidusic.staff.getChordList()) {
//
//				Set<Nota> notaSet = chord.getNotaSet().stream()
//						.filter(n -> n.getChannel() == channelFinal)
//						.collect(Collectors.toSet());
//
//				for (int i = 0; i < channlePhraseCount; ++i) {
//					if (has nota with this index) {
//						// добавить в соответствующую фразу
//					} else {
//						// добавить в фразу пазу, равную размеру аккорда (размеру минимальной ноты в аккорде)
//					}
//				}
//
//				for (Nota nota: notaSet) {
//
//					++i;
//				}
//
//				if (notaSet.size() > 0) {
//					phrase.addSet(notaSet);
//					// добавить ноту в фразу
//				} else {
//
//				}
//			}
//
//			if (phrase.getNoteList().stream().anyMatch(n -> !n.isPause())) {
//				result.add(phrase);
//			}
//		}

		return result.stream().filter(p -> p.getNoteList().size() > 0).collect(Collectors.toList());

	}

	private List<INota> getNoteList() {
		return this.noteList;
	}

	private static <T> Comparator<T> cmp(Function<T, Integer> getOrder) {
		return (a, b) -> getOrder.apply(a) - getOrder.apply(b);
	}

}
