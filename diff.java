diff --git a/build.sh b/build.sh
index c12aa73..14b1fe2 100755
--- a/build.sh
+++ b/build.sh
@@ -7,5 +7,5 @@ libs_query=$(join : libs/*)
 
 mkdir -p bin
 
-javac -sourcepath "./src" -d "./bin" "./src/main/Main.java" -cp $libs_query
+javac -sourcepath "./src" -d "./bin" "./src/org/sheet_midusic/stuff/main/Main.java" -cp $libs_query
 cp -R src/org/sheet_midusic/stuff/graphics/imgs bin/org/sheet_midusic/stuff/graphics/
diff --git a/run.sh b/run.sh
index 398b134..0d14b75 100755
--- a/run.sh
+++ b/run.sh
@@ -7,6 +7,6 @@ cd bin
 
 libs_query="$(join : ../libs/*):."
 
-java -cp $libs_query main.Main
+java -cp $libs_query org.sheet_midusic.stuff.main.Main
 
 cd ..
diff --git a/src/org/json b/src/org/json
--- a/src/org/json
+++ b/src/org/json
@@ -1 +1 @@
-Subproject commit ac1be561af48f349c3d855801f7673217f5c07c3
+Subproject commit ac1be561af48f349c3d855801f7673217f5c07c3-dirty
diff --git a/src/org/klesun_model/AbstractHandler.java b/src/org/klesun_model/AbstractHandler.java
index fd6827f..1d3a4f9 100755
--- a/src/org/klesun_model/AbstractHandler.java
+++ b/src/org/klesun_model/AbstractHandler.java
@@ -16,18 +16,6 @@ abstract public class AbstractHandler implements KeyListener, MouseListener, Mou
 
 	private IComponent context = null;
 
-	private LinkedList<SimpleAction> simpleActionQueue = new LinkedList<>();
-	private int simpleActionIterator = 0;
-
-	synchronized final public void performAction(SimpleAction action) {
-		if (simpleActionIterator < simpleActionQueue.size()) {
-			this.simpleActionQueue = new LinkedList<>(simpleActionQueue.subList(0, simpleActionIterator));
-		}
-		simpleActionQueue.addLast(action);
-		action.redo();
-		++simpleActionIterator;
-	}
-
 	// mouse
 	protected Point mouseLocation = new Point(0,0);
 
diff --git a/src/org/klesun_model/IComponent.java b/src/org/klesun_model/IComponent.java
index 07f5595..1a51481 100755
--- a/src/org/klesun_model/IComponent.java
+++ b/src/org/klesun_model/IComponent.java
@@ -1,5 +1,7 @@
 package org.klesun_model;
 
+import org.sheet_midusic.stuff.graphics.Settings;
+
 import java.awt.*;
 import java.awt.event.FocusListener;
 
@@ -30,4 +32,11 @@ public interface IComponent
 	void addFocusListener(FocusListener focusListener);
 
 	// </editor-fold>
+
+	default int dx() {
+		return Settings.inst().getStepWidth();
+	}
+	default int dy() {
+		return Settings.inst().getStepHeight();
+	}
 }
diff --git a/src/org/sheet_midusic/staff/Staff.java b/src/org/sheet_midusic/staff/Staff.java
index 5af9f24..9718fc7 100755
--- a/src/org/sheet_midusic/staff/Staff.java
+++ b/src/org/sheet_midusic/staff/Staff.java
@@ -171,14 +171,12 @@ public class Staff extends AbstractModel
 		return chordList.stream();
 	}
 
-	// TODO: makes leak rows
-	public List<List<Chord>> getAccordRowList(int width)
+	public List<List<Chord>> getAccordRowList(int rowSize)
 	{
 		List<List<Chord>> resultList = new ArrayList<>();
 
-		int rowSize = getAccordInRowCount(width);
 		for (int fromIdx = 0; fromIdx < this.getChordList().size(); fromIdx += rowSize) {
-			int toIndex = Math.min(fromIdx + getAccordInRowCount(width), this.getChordList().size());
+			int toIndex = Math.min(fromIdx + rowSize, this.getChordList().size());
 			resultList.add(this.getChordList().subList(fromIdx, toIndex));
 		}
 
@@ -187,11 +185,6 @@ public class Staff extends AbstractModel
 		return resultList;
 	}
 
-	// TODO: should be moved to StaffComponent
-	public int getHeightIf(int width) {
-		return getAccordRowList(width).size() * SISDISPLACE * dy() + getMarginY();
-	}
-
 	public int getMarginX() {
 		return dx();
 	}
@@ -199,12 +192,6 @@ public class Staff extends AbstractModel
 		return Math.round(MainPanel.MARGIN_V * dy());
 	}
 
-	// TODO: it shoud be component's method
-	public int getAccordInRowCount(int width) {
-		int result = width / (dx() * 2) - 3; // - 3 because violin key and phantom
-		return Math.max(result, 1);
-	}
-
 	final private int dx() { return Settings.inst().getStepWidth(); }
 	final private int dy() { return Settings.inst().getStepHeight(); }
 
diff --git a/src/org/sheet_midusic/staff/StaffHandler.java b/src/org/sheet_midusic/staff/StaffHandler.java
index 69af7e5..5ab066c 100755
--- a/src/org/sheet_midusic/staff/StaffHandler.java
+++ b/src/org/sheet_midusic/staff/StaffHandler.java
@@ -46,8 +46,8 @@ public class StaffHandler extends AbstractHandler {
 			.p(new Combo(ctrl, k.VK_RIGHT), mkAction(p -> p.moveFocusTact(1)).setCaption("Right Tact").setPostfix("Navigation"))
 			.p(new Combo(0, k.VK_LEFT), mkFailableAction(s -> s.moveFocusWithPlayback(-1)).setCaption("Left").setPostfix(navigation))
 			.p(new Combo(0, k.VK_RIGHT), mkFailableAction(s -> s.moveFocusWithPlayback(1)).setCaption("Right").setPostfix(navigation))
-			.p(new Combo(0, k.VK_UP), mkFailableAction(s -> s.moveFocusRow(-1, s.getWidth())).setCaption("Up").setPostfix(navigation))
-			.p(new Combo(0, k.VK_DOWN), mkFailableAction(s -> s.moveFocusRow(1, s.getWidth())).setCaption("Down").setPostfix(navigation))
+			.p(new Combo(0, k.VK_UP), mkFailableAction(s -> s.moveFocusRow(-1)).setCaption("Up").setPostfix(navigation))
+			.p(new Combo(0, k.VK_DOWN), mkFailableAction(s -> s.moveFocusRow(1)).setCaption("Down").setPostfix(navigation))
 
 			// TODO: move it to StaffConfig
 			.p(new Combo(ctrl, k.VK_D), mkFailableAction(s -> DeviceEbun.changeOutDevice(s.staff.getConfig()))
@@ -74,7 +74,8 @@ public class StaffHandler extends AbstractHandler {
 		}
 	}
 
-	public void handleMidiEvent(Integer tune, int forca, int timestamp) {
+	public void handleMidiEvent(Integer tune, int forca, int timestamp)
+	{
 		if (forca > 0) {
 			// BEWARE: we get sometimes double messages when my synt has "LAYER/AUTO HARMONIZE" button on. That is button, that makes one key press sound with two instruments
 			this.handleKey(new Combo(Combo.getAsciiTuneMods(), Combo.tuneToAscii(tune))); // (11 -ctrl+shift+alt)+someKey
diff --git a/src/org/sheet_midusic/staff/StaffPainter.java b/src/org/sheet_midusic/staff/StaffPainter.java
index 74036d7..a1e77ec 100755
--- a/src/org/sheet_midusic/staff/StaffPainter.java
+++ b/src/org/sheet_midusic/staff/StaffPainter.java
@@ -29,7 +29,7 @@ public class StaffPainter extends AbstractPainter
 		Staff.TactMeasurer tactMeasurer = new Staff.TactMeasurer(s.getConfig().getTactSize());
 
 		int i = 0;
-		for (java.util.List<Chord> row : s.getAccordRowList(comp.getWidth())) {
+		for (java.util.List<Chord> row : s.getAccordRowList(comp.getAccordInRowCount())) {
 
 			int y = i * Staff.SISDISPLACE * dy(); // bottommest y nota may be drawn on
 
@@ -39,11 +39,7 @@ public class StaffPainter extends AbstractPainter
 			for (Chord chord : row) {
 				int x = j * (2 * dx());
 
-//				ChordComponent chordComp = comp.findChild(chord);
-//				KeySignature siga = s.getConfig().getSignature();
-//				drawModel((g, xArg, yArg) -> chordComp.drawOn(g, xArg, yArg, siga), x, y - 12 * dy());
-
-				if (tactMeasurer.inject(chord)) { // TODO: broken. we cant draw it separately from chords (when you resize they go assync)
+				if (tactMeasurer.inject(chord)) {
 					drawTactLine(x + dx() * 2, y, tactMeasurer);
 				}
 
diff --git a/src/org/sheet_midusic/staff/chord/ChordComponent.java b/src/org/sheet_midusic/staff/chord/ChordComponent.java
index 696a05d..3598b47 100755
--- a/src/org/sheet_midusic/staff/chord/ChordComponent.java
+++ b/src/org/sheet_midusic/staff/chord/ChordComponent.java
@@ -32,6 +32,8 @@ public class ChordComponent extends JComponent implements IComponent
 		this.parent = parent;
 		this.chord = chord;
 		chord.notaList.get().forEach(this::addComponent);
+
+		this.addMouseListener(handler);
 	}
 
 	public NoteComponent addNewNota(int tune, int channel) {
diff --git a/src/org/sheet_midusic/staff/chord/ChordHandler.java b/src/org/sheet_midusic/staff/chord/ChordHandler.java
index 3dbe0ac..89e91f2 100755
--- a/src/org/sheet_midusic/staff/chord/ChordHandler.java
+++ b/src/org/sheet_midusic/staff/chord/ChordHandler.java
@@ -1,10 +1,7 @@
 
 package org.sheet_midusic.staff.chord;
 
-import org.klesun_model.AbstractHandler;
-import org.klesun_model.Combo;
-import org.klesun_model.ContextAction;
-import org.klesun_model.Explain;
+import org.klesun_model.*;
 import org.sheet_midusic.staff.chord.nota.Nota;
 import org.sheet_midusic.staff.chord.nota.NotaHandler;
 import org.sheet_midusic.staff.chord.nota.NoteComponent;
@@ -73,4 +70,11 @@ public class ChordHandler extends AbstractHandler {
 		ContextAction<ChordComponent> action = new ContextAction<>();
 		return action.setRedo(lambda);
 	}
+
+	@Override
+	public Boolean mousePressedFinal(ComboMouse combo)
+	{
+		getContext().getParentComponent().setFocus(getContext());
+		return true;
+	}
 }
diff --git a/src/org/sheet_midusic/staff/staff_panel/MainPanel.java b/src/org/sheet_midusic/staff/staff_panel/MainPanel.java
index 67a2362..4222fff 100755
--- a/src/org/sheet_midusic/staff/staff_panel/MainPanel.java
+++ b/src/org/sheet_midusic/staff/staff_panel/MainPanel.java
@@ -1,6 +1,5 @@
 package org.sheet_midusic.staff.staff_panel;
 
-import com.sun.istack.internal.NotNull;
 import org.sheet_midusic.stuff.main.Main;
 import org.sheet_midusic.staff.Staff;
 import org.sheet_midusic.stuff.graphics.Settings;
@@ -31,8 +30,8 @@ final public class MainPanel extends JPanel implements IComponent {
 	private Boolean surfaceCompletelyChanged = false;
 
 	/** @debug - return private when done */
-	@NotNull public SheetMusicComponent staffContainer = new SheetMusicComponent(new SheetMusic(), this);
-	@NotNull public Scroll staffScroll = new Scroll(staffContainer);
+	public SheetMusicComponent staffContainer = new SheetMusicComponent(new SheetMusic(), this);
+	public Scroll staffScroll = new Scroll(staffContainer);
 	final private PianoLayoutPanel pianoLayoutPanel;
 
 	public MainPanel() {
@@ -105,9 +104,6 @@ final public class MainPanel extends JPanel implements IComponent {
 		return Settings.inst();
 	}
 
-	// maybe put it into AbstractModel?
-	private int dy() { return getSettings().getStepHeight(); }
-
 	// Until here
 
 	// private methods
diff --git a/src/org/sheet_midusic/staff/staff_panel/SheetMusicComponent.java b/src/org/sheet_midusic/staff/staff_panel/SheetMusicComponent.java
index a631917..72de7ea 100755
--- a/src/org/sheet_midusic/staff/staff_panel/SheetMusicComponent.java
+++ b/src/org/sheet_midusic/staff/staff_panel/SheetMusicComponent.java
@@ -80,7 +80,12 @@ public class SheetMusicComponent extends JPanel implements IComponent
 
 	private int getFocusedSystemY() {
 		int dy = Settings.inst().getStepHeight();
-		return Staff.SISDISPLACE * dy * (getFocusedChild().staff.getFocusedIndex() / getFocusedChild().staff.getAccordInRowCount(getWidth()));
+		return Staff.SISDISPLACE * dy * (getFocusedChild().staff.getFocusedIndex() / getAccordInRowCount());
+	}
+
+	private int getAccordInRowCount() {
+		int result = getWidth() - StaffComponent.getLeftMargin() - StaffComponent.getRightMargin();
+		return Math.max(result, 1);
 	}
 
 	public void checkCam()
@@ -100,7 +105,7 @@ public class SheetMusicComponent extends JPanel implements IComponent
 	@Override
 	public Dimension getPreferredSize()
 	{
-		int height = getStaffPanelStream().map(c -> c.staff.getHeightIf(getWidth())).reduce(Math::addExact).get();
+		int height = getStaffPanelStream().map(c -> c.calcTrueHeight()).reduce(Math::addExact).get();
 		return new Dimension(mainPanel.getWidth() - 30, height); // - 30 - love awt and horizontal scrollbars
 	}
 
diff --git a/src/org/sheet_midusic/staff/staff_panel/StaffComponent.java b/src/org/sheet_midusic/staff/staff_panel/StaffComponent.java
index d1e77ed..b58d392 100755
--- a/src/org/sheet_midusic/staff/staff_panel/StaffComponent.java
+++ b/src/org/sheet_midusic/staff/staff_panel/StaffComponent.java
@@ -18,8 +18,9 @@ import org.sheet_midusic.stuff.tools.Logger;
 
 import javax.swing.*;
 import java.awt.*;
-import java.util.HashSet;
-import java.util.Set;
+import java.util.*;
+import java.util.List;
+import java.util.stream.Stream;
 
 // TODO: merge with AbstractPainter
 public class StaffComponent extends JPanel implements IComponent
@@ -29,7 +30,7 @@ public class StaffComponent extends JPanel implements IComponent
 
 	final private IComponent parent;
 	final private StaffHandler handler;
-	final private JPanel chordSpace = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
+	final public JPanel chordSpace = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
 	final private Playback playback;
 
 	public StaffComponent(Staff staff, IComponent parent) {
@@ -58,21 +59,21 @@ public class StaffComponent extends JPanel implements IComponent
 
 		JPanel leftGap = new JPanel() {
 			public Dimension getPreferredSize() {
-				return new Dimension(staff.getMarginX() * 4, 10);
+				return new Dimension(getLeftMargin(), 10);
 			}
 		};
 		leftGap.setOpaque(false); // temporary solution
 
 		JPanel rightGap = new JPanel() {
 			public Dimension getPreferredSize() {
-				return new Dimension(staff.getMarginX() * 3, 10);
+				return new Dimension(getRightMargin(), 10);
 			}
 		};
 		rightGap.setOpaque(false); // temporary solution
 
 		JPanel topGap = new JPanel() { // TODO: deeds are wrong with this margin y - chords drawn wrong place'
 			public Dimension getPreferredSize() {
-				return new Dimension(10, staff.getMarginY() - 12 * Settings.inst().getStepHeight());
+				return new Dimension(10, getTopMargin());
 			}
 		};
 		topGap.setOpaque(false); // temporary solution
@@ -85,6 +86,19 @@ public class StaffComponent extends JPanel implements IComponent
 		chordSpace.setOpaque(false);
 	}
 
+	/** @return - pixel count */
+	public static int getLeftMargin() {
+		return Settings.inst().getStepWidth() * 4;
+	}
+	/** @return - pixel count */
+	public static int getRightMargin() {
+		return Settings.inst().getStepWidth() * 3;
+	}
+	/** @return - pixel count */
+	public static int getTopMargin() {
+		return Settings.inst().getStepHeight() * 3;
+	}
+
 	public ChordComponent addNewChordWithPlayback()
 	{
 		Chord chord = staff.addNewAccord(staff.getFocusedIndex() + 1);
@@ -188,8 +202,8 @@ public class StaffComponent extends JPanel implements IComponent
 		return new Explain(false, "Not Implemented Yet!");
 	}
 
-	public Explain moveFocusRow(int sign, int width) {
-		int n = sign * staff.getAccordInRowCount(width);
+	public Explain moveFocusRow(int sign) {
+		int n = sign * getAccordInRowCount();
 		return moveFocusWithPlayback(n);
 	}
 
@@ -202,11 +216,46 @@ public class StaffComponent extends JPanel implements IComponent
 			findChild(staff.getFocusedAccord()).repaint();
 		}
 		if (wasIndex != -1) {
-			findChild(staff.getChordList().get(wasIndex)).repaint(); // TODO: with Home/End not cleaned and piano grephic layout should be repainted each time
+			findChild(staff.getChordList().get(wasIndex)).repaint();
+			// TODO: with Home/End not cleaned and
+			// TODO: piano grephic layout should be repainted each time and
+			// TODO: cam should be checked
 		}
 
 		return staff.getFocusedIndex() != wasIndex
-				? new Explain(true)
-				: new Explain(false, "dead end").setImplicit(true);
+			? new Explain(true)
+			: new Explain(false, "dead end").setImplicit(true);
+	}
+
+	public StaffComponent setFocus(ChordComponent comp) {
+		return setFocus(staff.getChordList().indexOf(comp.chord));
+	}
+
+	public StaffComponent setFocus(int index)
+	{
+		ChordComponent was = getFocusedChild();
+		staff.setFocusedIndex(index);
+
+		if (getFocusedChild() != null) {
+
+			PlayMusThread.shutTheFuckUp();
+			playback.interrupt();
+			PlayMusThread.playAccord(staff.getFocusedAccord());
+		}
+
+		if (was != null) {
+			was.repaint();
+		}
+		getFocusedChild().repaint();
+		return this;
+	}
+
+	public int getAccordInRowCount() {
+		int result = chordSpace.getWidth() / (dx() * 2);
+		return Math.max(result, 1);
+	}
+
+	public int calcTrueHeight() {
+		return staff.getAccordRowList(getAccordInRowCount()).size() * Staff.SISDISPLACE * dy();
 	}
 }
diff --git a/src/org/sheet_midusic/stuff/main/Main.java b/src/org/sheet_midusic/stuff/main/Main.java
index 92941ed..62ae088 100755
--- a/src/org/sheet_midusic/stuff/main/Main.java
+++ b/src/org/sheet_midusic/stuff/main/Main.java
@@ -7,9 +7,8 @@ import org.sheet_midusic.stuff.tools.Logger;
 public class Main
 {
 	/* TODO: maybe rename midiana to something like
-	 * SheetMidiMusic,
-	 * SheetMidusic (and portrait of woman with snakes from head on main page)
-	 * ShmiditMusic
+	 * shmidusic
+	 * shmidi
 	 */
 
 	public static Boolean isLinux = false;
