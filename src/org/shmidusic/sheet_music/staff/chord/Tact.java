package org.shmidusic.sheet_music.staff.chord;

import org.apache.commons.math3.fraction.Fraction;
import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;

import java.util.ArrayList;

// Tact is chord set from one Tact line to Another (see TactMeasurer)
public class Tact extends AbstractModel
{
	/** should contain what left from previous tact */
	private Fraction precedingRest = new Fraction(0);

	public Arr<Chord> accordList = new Arr<>("chordList", new ArrayList<>(), this, Chord.class);
	public Field<Integer> tactNumber = new Field<>("tactNumber", Integer.class, true, this);

	public Tact(int tactNumber)
	{
		this.tactNumber.set(tactNumber);
	}

	// TODO: don't you think it would be better if it was final field?

	public Tact setPrecedingRest(Fraction value) {
		this.precedingRest = value;
		return this;
	}

	public Boolean getIsCorrect() {
		return this.precedingRest.equals(new Fraction(0));
	}

	public Fraction getPrecedingRest() {
		return this.precedingRest;
	}
}
