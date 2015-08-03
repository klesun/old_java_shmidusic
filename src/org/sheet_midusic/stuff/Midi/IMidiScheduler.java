package org.sheet_midusic.stuff.Midi;

import org.apache.commons.math3.fraction.Fraction;
import org.sheet_midusic.stuff.tools.jmusic_integration.INota;

public interface IMidiScheduler
{
	void addNoteTask(Fraction when, INota nota);
}
