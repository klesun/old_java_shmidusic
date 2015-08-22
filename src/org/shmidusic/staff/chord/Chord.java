package org.shmidusic.staff.chord;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.klesun_model.AbstractModel;
import org.klesun_model.field.Arr;
import org.klesun_model.field.Field;
import org.shmidusic.stuff.tools.jmusic_integration.INota;
import org.apache.commons.math3.fraction.Fraction;

import org.shmidusic.staff.chord.nota.Nota;
import org.json.JSONObject;

public class Chord extends AbstractModel
{
	private Field<Boolean> isDiminendo = new Field<>("isDiminendo", false, this).setPaintingLambda(ChordPainter::diminendoPainting);
	public Field<String> slog = new Field<>("slog", "", this).setPaintingLambda(ChordPainter::slogPainting).setOmitDefaultFromJson(true);
	public Arr<Nota> notaList = new Arr<>("notaList", new TreeSet<>(), this, Nota.class);

	// getters/setters

	public TreeSet<Nota> getNotaSet() {
		return (TreeSet)notaList.get();
	}

	public Stream<Nota> notaStream(Predicate<Nota> filterLambda) {
		return getNotaSet().stream().filter(filterLambda);
	}

	public Stream<Nota> notaStream() {
		return getNotaSet().stream();
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

	// field getters/setters

	public String getSlog() { return this.slog.get(); }
	public Chord setSlog(String value) { this.slog.set(value); return this; }

	public Boolean getIsDiminendo() { return isDiminendo.get(); }
	public void setIsDiminendo(Boolean value) { isDiminendo.set(value); }

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
		notaList.remove(nota);
	}

}