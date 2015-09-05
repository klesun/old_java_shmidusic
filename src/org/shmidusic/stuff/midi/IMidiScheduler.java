package org.shmidusic.stuff.midi;

import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.tools.INota;

public interface IMidiScheduler
{
	void addNoteTask(Fraction when, INota nota);
}
