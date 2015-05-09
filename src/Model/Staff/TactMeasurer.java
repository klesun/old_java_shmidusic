package Model.Staff;

import Model.Staff.Accord.Accord;
import org.apache.commons.math3.fraction.Fraction;

public class TactMeasurer {

	private Staff parent = null;

	public Fraction sumFraction = new Fraction(0);

	public int tactCount = 0;

	public TactMeasurer(Staff parent) {
		this.parent = parent;
	}

	/** @returns true if accord finished the tact */
	public Boolean inject(Accord accord) {
		sumFraction = sumFraction.add(accord.getShortestFraction());

		// TODO: replace with config.getFraction()
		int tactNumerator = parent.getConfig().numerator * 8;
		int tactDenominator = Staff.DEFAULT_ZNAM;
		Fraction tactFraction = new Fraction(tactNumerator, tactDenominator);

		if (sumFraction.compareTo(tactFraction) >= 0) {
			sumFraction = sumFraction.subtract(tactFraction);
			++tactCount;
			return true;
		}
		else
		{
			return false;
		}
	}
}
