package org.shmidusic.stuff.tools.jmusic_integration.JmModel;

import org.shmidusic.sheet_music.staff.Staff;
import org.jm.music.data.Score;

public class JmScoreMaker {

	public static Score makeFrom(Staff staff) {
		Score score = new Score();
		score.setTempo(staff.getConfig().getTempo());

		score.addPartList(JmPartMaker.makeListFrom(staff));

		return score;
	}

}
