package Stuff.Tools.jmusicIntegration;

import Gui.ImageStorage;
import Model.Helper;
import Stuff.Tools.Bin;
import Stuff.Tools.Logger;
import org.apache.commons.math3.fraction.Fraction;

public interface INota extends Comparable<INota> {

	Integer getTune();
	Integer getChannel();
	Fraction getLength();
	Boolean isTriplet();

	default int compareTo(INota n) {
		return ((n.getTune() - this.getTune()) << 4) + (n.getChannel() - this.getChannel());
	}

	static Fraction legnthNorm(Fraction value) {
		if (!isDotable(value)) {
			Logger.warning("Not Dotable Fraction: " + value);
		}
		return Helper.limit(value, new Fraction(1, 256), new Fraction(4)); // sometimes need to fill diff between triplet and straight
//		return Helper.limit(value, new Fraction(1, 16), new Fraction(2));
	}

	default Boolean isDotable() {
		return isDotable(getLength());
	}

	// 1/256 + 1/128 + 1/64 + 1/32 + 1/16 + 1/8 + 1/4 + 1/2 = 1111 1111

	// 0000 0111 - true

	// 0001 1111 - true

	// 0011 1111 - true

	// 0010 0110 - false

	/** @return true if length can be expressed through sum 2 + 1 + 1/2 + 1/4 + 1/8 + ... 1/2^n */
	static Boolean isDotable(Fraction length) {
		length = length.divide(ImageStorage.getGreatestPossibleNotaLength().multiply(2)); // to make sure, that the length is less than 1
		if (length.compareTo(new Fraction(1)) >= 0) {
			Logger.fatal("Providen fraction is greater than 1 even after deviding it to gretest possible Nota length! We'll all die!!!" + length);
		}

		if (Bin.isPowerOf2(length.getDenominator())) { // will be false for triols
			return Bin.isPowerOf2(length.getNumerator() + 1); // not sure... but, ok, kinda sure
		} else {
			return false;
		}
	}

}
