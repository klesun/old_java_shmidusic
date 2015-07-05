package BlockSpacePkg.StaffPkg;

import BlockSpacePkg.StaffPkg.Accord.Accord;
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

		Boolean finishedTact = false;
		while (sumFraction.compareTo(parent.getConfig().getTactSize()) >= 0) {
			sumFraction = sumFraction.subtract(parent.getConfig().getTactSize());
			++tactCount;
			finishedTact = true;
		}

		return finishedTact;
	}
}
