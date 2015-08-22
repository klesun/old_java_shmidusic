package org.shmidusic.stuff.tools.jmusic_integration;

import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONObject;

public class JmNote implements INota {

	final private int pitch;
	final public Double rawLength;
	final private Boolean isTriplet;
	final private int channel;

	public JmNote(int pitch, Double rawLength, int channel) {
		this.pitch = pitch;
		this.channel = channel;

		if (toFrac(rawLength).getDenominator() % 3 == 0) { // TODO it's not fair. nota with three dots will match and with 5 and with 1
			this.isTriplet = true;
			this.rawLength = rawLength * 3;
		} else {
			this.isTriplet = false;
			this.rawLength = rawLength;
		}
	}

	// sorry for confusion. JMusic stores lengths divided by 4 so quarter was 1/1, lets stick to them
	public static JmNote mk(int pitch, Fraction length, int channel) {
		return new JmNote(pitch, length.multiply(4).doubleValue(), channel);
	}

	public static JmNote mk(JSONObject js, int channel) {

		int tune = js.has("pitch")
				? js.getInt("pitch")
				: JM_DEFAULT_PITCH; // NARKOMANI

		Double rhythm = js.has("rhythmValue")
				? js.getDouble("rhythmValue")
				: JM_DEFAULT_RHYTM; // narkomani

		return new JmNote(tune, rhythm, channel);
	}

	@Override
	public Integer getTune() {
		return this.pitch;
	}

	@Override
	public Integer getChannel() {
		// TODO: actually, it's a lie, channel is stored in Part in org.jm
		return this.channel;
	}

	@Override
	public Boolean isTriplet() {
		return isTriplet;
	}

	// taking isTriplet to account... TODO: maybe we should include triplet into frac after all
	public Fraction getRealLength() {
		return getLength().divide(isTriplet ? 3 : 1);
	}

	// TODO: check that num corresponds the rule "denominator is pow of 2 and numenator so that only dots"
	// cuz im aware they could store triplets here as well (i hope not, but have no midi with triplets to test)
	@Override
	public Fraction getLength() {
		return toFrac(rawLength);
	}

	public static Fraction toFrac(Double rawLength) {
		Double realNum = Math.round(rawLength / 4 * 100000) / 100000.0; // will break program if got triplet and yu-no this
		return new Fraction(realNum/*, 48*/); // 48 - 1/48 16th triol
	}

	final private static Double JM_DEFAULT_RHYTM = 1.0; // quarter... narkomani
	final private static Integer JM_DEFAULT_PITCH = 60; // quarter... NARKOMANI
}
