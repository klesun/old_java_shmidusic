package org.shmidusic.sheet_music.staff.chord;

import com.google.common.collect.Lists;
import org.apache.commons.math3.fraction.Fraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Tact is chord set from one Tact line to Another (see TactMeasurer)
public class Tact
{
	/** should contain what left from previous tact */
	private Fraction precedingRest = new Fraction(0);
    final private Fraction tactSize;

	public List<Chord> chordList = new ArrayList<>();

	public Tact(Fraction tactSize)
	{
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
        for (Chord chord: chordList) {

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
        for (Chord chord: Lists.reverse(chordList)) {

            curPos = chordPos.subtract(chord.getFraction());

            if (curPos.compareTo(chordPos) < 0) {
                result = Optional.of(chord);
                break;
            }
        }

        return result;
    }
}
