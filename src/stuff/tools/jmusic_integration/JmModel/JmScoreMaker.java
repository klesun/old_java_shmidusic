package stuff.tools.jmusic_integration.JmModel;

import blockspace.staff.Staff;
import jm.music.data.Part;
import jm.music.data.Score;

public class JmScoreMaker {

	public static Score makeFrom(Staff staff) {
		Score score = new Score();
		score.setTempo(staff.getConfig().getTempo());

		score.addPartList(JmPartMaker.makeListFrom(staff));

		return score;
	}

}
