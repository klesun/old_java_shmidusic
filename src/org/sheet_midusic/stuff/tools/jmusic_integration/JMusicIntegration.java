package org.sheet_midusic.stuff.tools.jmusic_integration;

import org.sheet_midusic.staff.Staff;
import org.klesun_model.Explain;
import org.json.JSONArray;
import org.json.JSONObject;

public class JMusicIntegration {

	final private Staff staff;

	public JMusicIntegration(Staff staff) {
		this.staff = staff;
	}

	public Explain fillFromJm(JSONObject jmJs) {
		return fillFromJm(jmJs, false);
	}

	// with rounding
	public Explain fillFromJm2(JSONObject jmJs) {
		return fillFromJm(jmJs, true);
	}

	private Explain fillFromJm(JSONObject jmJs, Boolean round) {
		int channel = 0;

		JSONArray partList = jmJs.getJSONArray("partList");
		for (int i = 0; i < partList.length(); ++i) {
			JSONArray phraseList = partList.getJSONObject(i).getJSONArray("phraseList");
			for (int j = 0; j < phraseList.length(); ++j) {
				new AddPhrase(staff, phraseList.getJSONObject(j), channel++, round).perform();

				/** @debug - wanna listen just how first sound */
//				break;
			}

			/** @debug - lets merge just phrases for begin */
//			break;
		}

		return new Explain(true);
	}
}

