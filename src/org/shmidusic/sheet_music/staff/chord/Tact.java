package org.shmidusic.sheet_music.staff.chord;

import com.google.common.collect.Lists;
import org.apache.commons.math3.fraction.Fraction;
import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;

import java.util.ArrayList;
import java.util.Optional;

// Tact is chord set from one Tact line to Another (see TactMeasurer)
public class Tact extends AbstractModel
{
	/** should contain what left from previous tact */
	private Fraction precedingRest = new Fraction(0);
    final private Fraction tactSize;

    public Field<Integer> tactNumber = new Field<>("tactNumber", Integer.class, true, this);
	public Arr<Chord> chordList = new Arr<>("chordList", new ArrayList<>(), this, Chord.class);

	public Tact(int tactNumber, Fraction tactSize)
	{
		this.tactNumber.set(tactNumber);
        this.tactSize = tactSize;
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

    public Optional<Chord> findChord(Fraction chordPos)
    {
        Optional<Chord> result = Optional.empty();

        Fraction curPos = new Fraction(0);
        for (Chord chord: chordList.get()) {

            if (curPos.equals(chordPos)) {
                result = Optional.of(chord);
                break;
            }
            curPos = curPos.add(chord.getFraction());
        }

        return result;
    }

    public Optional<Chord> findClosestBefore(Fraction chordPos)
    {
        Optional<Chord> result = Optional.empty();

        Fraction curPos = tactSize;
        for (Chord chord: Lists.reverse(new ArrayList<>(chordList.get()))) {

            curPos = chordPos.subtract(chord.getFraction());

            if (curPos.compareTo(chordPos) < 0) {
                result = Optional.of(chord);
                break;
            }
        }

        return result;
    }
}
