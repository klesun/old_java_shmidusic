package org.sheet_midusic.staff.chord;

import java.awt.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.klesun_model.AbstractModel;
import org.klesun_model.Explain;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;
import org.klesun_model.SimpleAction;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.staff_panel.StaffComponent;
import org.sheet_midusic.stuff.tools.jmusic_integration.INota;
import org.apache.commons.math3.fraction.Fraction;

import org.sheet_midusic.staff.chord.nota.Nota;
import org.sheet_midusic.staff.Staff;
import org.json.JSONObject;

public class Chord extends AbstractModel
{
	private Field<Boolean> isDiminendo = new Field<>("isDiminendo", false, this).setPaintingLambda(ChordPainter::diminendoPainting);
	public Field<String> slog = new Field<>("slog", "", this).setPaintingLambda(ChordPainter::slogPainting).setOmitDefaultFromJson(true);
	public Arr<Nota> notaList = new Arr<>("notaList", new TreeSet<>(), this, Nota.class);

	private Boolean surfaceChanged = true;

	int focusedIndex = -1;

	public Chord() {
		h.getFieldStorage().forEach(f -> f.setOnChange(this::surfaceChanged));
	}

	public void surfaceChanged() {
		this.surfaceChanged = true;
	}

	// responses to events (actions)

	public Explain<Boolean> moveFocus(int n) {

		if (getFocusedIndex() + n > this.getNotaSet().size() - 1 || getFocusedIndex() + n < 0) {
			this.setFocusedIndex(-1);
			return new Explain<>(false, "End Of chord");
		} else {
			if (this.getFocusedIndex() + n < -1) {
				this.setFocusedIndex(this.getNotaSet().size() - 1);
			} else {
				this.setFocusedIndex(this.getFocusedIndex() + n);
			}
			return new Explain<>(true);
		}
	}

	// getters/setters

	public TreeSet<Nota> getNotaSet() {
		return (TreeSet)notaList.get();
	}

	public Stream<Nota> notaStream(Predicate<Nota> filterLambda) {
		return getNotaSet().stream().filter(filterLambda);
	}

	public long getEarliestKeydown() {
		Nota nota = this.getNotaSet().stream().reduce(null, (a, b) -> a != null && a.keydownTimestamp < b.keydownTimestamp ? a : b);
		return nota != null ? nota.keydownTimestamp : 0;
	}

	public Nota findByTuneAndChannel(int tune, int channel) {
		return this.getNotaSet().stream().filter(n -> n.tune.get() == tune && n.getChannel() == channel).findFirst().orElse(null);
	}

	public int getShortestTime(int tempo) {
		return Nota.getTimeMilliseconds(getFraction(),tempo);
	}

	public Fraction getFraction() {
		Nota nota = this.getNotaSet().stream().reduce(null, (a, b) -> a != null && !a.isLongerThan(b) && !a.getIsMuted() ? a : b);
		return nota != null ? nota.getRealLength() : new Fraction(0);
	}

	synchronized public Nota getFocusedNota() {
		return getFocusedIndex() > -1 ? this.notaList.get(getFocusedIndex()) : null;
	}

	// field getters/setters

	public String getSlog() { return this.slog.get(); }
	public Chord setSlog(String value) { this.slog.set(value); return this; }
	public int getFocusedIndex() {
		return this.focusedIndex;
	}

	public Boolean getIsDiminendo() { return isDiminendo.get(); }
	public void setIsDiminendo(Boolean value) { isDiminendo.set(value); }

	public void triggerIsDiminendo() {
		setIsDiminendo(!getIsDiminendo());
	}

	public Chord setFocusedIndex(int value) {
		value = value >= this.getNotaSet().size() ? this.getNotaSet().size() - 1 : value;
		value = value < -1 ? -1 : value;
		this.focusedIndex = value;
		return this;
	}

	@Deprecated // it's ChordComponent's logic
	public Nota getFocusedChild() {
		return this.getFocusedNota();
	}

	// event handles

	public Nota addNewNota(INota source) {
		Nota newNota = addNewNota(source.getTune(), source.getChannel()).setLength(source.getLength());
		newNota.isTriplet.set(source.isTriplet());
		return newNota;
	}

	public Nota addNewNota(int tune, int channel) {
		return add(new Nota().setTune(tune).setChannel(channel)
			.setKeydownTimestamp(System.currentTimeMillis()));
	}

	public Nota addNewNota(JSONObject newNotaJs) {
		return add(new Nota().reconstructFromJson(newNotaJs)
			.setKeydownTimestamp(System.currentTimeMillis()));
	}

	synchronized public Nota add(Nota nota) {
		return notaList.add(nota);
	}

	synchronized public void remove(Nota nota) {
		int index = getNotaSet().headSet(nota).size();
		if (index <= getFocusedIndex()) { setFocusedIndex(getFocusedIndex() - 1); }

		notaList.remove(nota);
	}

}
