package blockspace.staff.accord;

import java.awt.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import model.Explain;
import model.field.Arr;
import model.field.Field;
import model.SimpleAction;
import blockspace.staff.MidianaComponent;
import stuff.tools.jmusic_integration.INota;
import org.apache.commons.math3.fraction.Fraction;

import blockspace.staff.accord.nota.Nota;
import blockspace.staff.Staff;
import org.json.JSONObject;

public class Chord extends MidianaComponent
{
	private Field<Boolean> isDiminendo = new Field<>("isDiminendo", false, this).setPaintingLambda(ChordPainter::diminendoPainting);
	public Field<String> slog = new Field<>("slog", "", this).setPaintingLambda(ChordPainter::slogPainting).setOmitDefaultFromJson(true);
	public Arr<Nota> notaList = new Arr<>("notaList", new TreeSet<>(), this, Nota.class);

	private Boolean surfaceChanged = true;

	int focusedIndex = -1;

	public Chord(Staff parent) {
		super(parent);
		h.getFieldStorage().forEach(f -> f.setOnChange(this::surfaceChanged));
	}

	public void surfaceChanged() {
		this.surfaceChanged = true;
	}

	public void drawOn(Graphics2D surface, int x, int y, Boolean completeRepaintRequired) {
		if (completeRepaintRequired || surfaceChanged) {
			new ChordPainter(this, surface, x, y).draw(true); // TODO: make it be not needed
			surfaceChanged = false;
		}
	}

	// responses to events (actions)

	public Explain<Boolean> moveFocus(int n) {

		if (getFocusedIndex() + n > this.getNotaSet().size() - 1 || getFocusedIndex() + n < 0) {
			this.setFocusedIndex(-1);
			return new Explain<>(false, "End Of accord");
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

	public int getHeight() {
		return this.getLowestPossibleNotaY();
	}

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

	public int getShortestTime() {
		return Nota.getTimeMilliseconds(getFraction(), getParentStaff().getConfig().getTempo());
	}

	public Fraction getFraction() {
		Nota nota = this.getNotaSet().stream().reduce(null, (a, b) -> a != null && !a.isLongerThan(b) && !a.getIsMuted() ? a : b);
		return nota != null ? nota.getRealLength() : new Fraction(0);
	}

	synchronized public Nota getFocusedNota() {
		return getFocusedIndex() > -1 ? this.notaList.get(getFocusedIndex()) : null;
	}

	public int getLowestPossibleNotaY() {
		return 50 * dy();
	}

	// field getters/setters

	public Staff getParentStaff() {
		return (Staff)this.getModelParent();
	}
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

	public Chord getNext() {
		int nextIndex = getParentStaff().getChordList().indexOf(this) + 1;
		return nextIndex < getParentStaff().getChordList().size()
				? getParentStaff().getChordList().get(nextIndex)
				: null;
	}

	public Chord setFocusedIndex(int value) {
		value = value >= this.getNotaSet().size() ? this.getNotaSet().size() - 1 : value;
		value = value < -1 ? -1 : value;
		this.focusedIndex = value;
		return this;
	}

	@Override
	public Nota getFocusedChild() {
		return this.getFocusedNota();
	}
	@Override
	protected AccordHandler makeHandler() {
		return new AccordHandler(this);
	}

	// event handles

	public Nota addNewNota(INota source) {
		Nota newNota = addNewNota(source.getTune(), source.getChannel()).setLength(source.getLength());
		newNota.isTriplet.set(source.isTriplet());
		return newNota;
	}

	public Nota addNewNota(int tune, int channel) {
		return add(new Nota(this).setTune(tune).setChannel(channel)
			.setKeydownTimestamp(System.currentTimeMillis()));
	}

	public Nota addNewNota(JSONObject newNotaJs) {
		return add(new Nota(this).reconstructFromJson(newNotaJs)
			.setKeydownTimestamp(System.currentTimeMillis()));
	}

	synchronized public Nota add(Nota nota) {
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> notaList.add(nota))
			.setUndo(() -> notaList.remove(nota)));
		return nota;
	}

	synchronized public void remove(Nota nota) {
		int index = getNotaSet().headSet(nota).size();
		if (index <= getFocusedIndex()) { setFocusedIndex(getFocusedIndex() - 1); }
		getHandler().performAction(new SimpleAction()
			.setRedo(() -> notaList.remove(nota))
			.setUndo(() -> notaList.add(nota)));
	}

}
