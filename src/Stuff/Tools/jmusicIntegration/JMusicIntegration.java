package Stuff.Tools.jmusicIntegration;

import BlockSpacePkg.StaffPkg.Accord.Nota.Nota;
import BlockSpacePkg.StaffPkg.Staff;
import Gui.ImageStorage;
import Model.Explain;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JMusicIntegration {

	final private Staff staff;

	public JMusicIntegration(Staff staff) {
		this.staff = staff;
	}

	public Explain fillFromJm(JSONObject jmJs) {

		int channel = 0;

		JSONArray partList = jmJs.getJSONArray("partList");
		for (int i = 0; i < partList.length(); ++i) {
			JSONArray phraseList = partList.getJSONObject(i).getJSONArray("phraseList");
			for (int j = 0; j < phraseList.length(); ++j) {
				new AddPhrase(staff, phraseList.getJSONObject(j), channel++).perform();
			}

			/** @debug - lets merge just phrases for begin */
//			break;
		}

		return new Explain(true);
	}
}

