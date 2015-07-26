package stuff.Midi;

import org.apache.commons.math3.fraction.Fraction;
import stuff.tools.jmusic_integration.INota;

public interface IMidiScheduler
{
	void addNoteOnTask(Fraction when, INota nota);
	void addNoteOffTask(Fraction when, INota nota);
}
