package org.sheet_midusic.staff.staff_panel;

import org.klesun_model.*;
import org.sheet_midusic.staff.Staff;
import org.sheet_midusic.staff.chord.Chord;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;
import org.sheet_midusic.stuff.tools.FileProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class SheetMusicPanel extends JPanel implements IComponent
{
	@Deprecated final MainPanel mainPanel;
	final public SheetMusic sheetMusic;
	final AbstractHandler handler;

	private Set<StaffComponent> staffComponentSet = new HashSet<>();

	public SheetMusicPanel(@Deprecated MainPanel mainPanel)
	{
		this.sheetMusic = new SheetMusic();
		Staff staff = new Staff(this.mainPanel = mainPanel);
		this.sheetMusic.staffList.add(staff);
		this.staffComponentSet.add(new StaffComponent(staff));

		this.handler = new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
				return new TruMap<>()
					.p(new Combo(ctrl, k.VK_L), mkFailableAction(SheetMusicPanel::splitFocusedStaff))
					.p(new Combo(ctrl, k.VK_P), mkAction(SheetMusicPanel::triggerPlayback).setCaption("Play/Stop"))
						// File
					.p(new Combo(ctrl, k.VK_S), mkFailableAction(FileProcessor::saveMusicPanel).setCaption("Save midi.json"))
					.p(new Combo(ctrl, k.VK_U), mkFailableAction(FileProcessor::saveMidi).setCaption("Save midi (alpha)"))
					.p(new Combo(ctrl, k.VK_O), mkFailableAction(FileProcessor::openStaff).setCaption("Open"))
					.p(new Combo(ctrl, k.VK_E), mkFailableAction(FileProcessor::savePNG).setCaption("Export png"))
					;
			}
		};

		this.setBackground(Color.WHITE);
	}

	public void triggerPlayback() {
		getFocusedChild().staff.getPlayback().trigger();
	}

	/** creating two staffs from one: to pointer pos and from pointer pos */
	public Explain splitFocusedStaff()
	{
		Staff s = getFocusedChild().staff;
		return new Explain(s.getFocusedIndex() > 0, "im not plitting here").runIfSuccess(() -> {
			Staff newStaff = this.sheetMusic.addNewStaffAfter(s);

			List<Chord> staff2Chords = s.getChordList().subList(s.getFocusedIndex(), s.getChordList().size());

			staff2Chords.stream().forEach(c -> {
				s.remove(c);
				newStaff.addNewAccord().reconstructFromJson(c.getJsonRepresentation());
			});

			newStaff.getConfig().reconstructFromJson(newStaff.getJsonRepresentation());
		});
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g;

		// TODO: i suspect it is the reason of huge lags in Linux. Maybe disable it only for him ? =D
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		getStaffPanelStream().reduce(0, (y, s) -> y + s.drawOn(g2, 0, y), Integer::sum);
	}

	private Stream<StaffComponent> getStaffPanelStream()
	{
		return sheetMusic.staffList.get().stream().map(this::getComponentByStaff);
	}

	private StaffComponent getComponentByStaff(Staff staff)
	{
		// TODO: change Set to Map for performance
		return staffComponentSet.stream().filter(p -> p.staff == staff).findAny().get();
	}

	@Override
	public IComponent getModelParent() {
		return this.mainPanel;
	}

	@Override
	public StaffComponent getFocusedChild() {
		// TODO: make changing focus possible
		return getComponentByStaff(sheetMusic.staffList.get(0));
	}

	@Override
	public AbstractHandler getHandler() {
		return this.handler;
	}

//		context.getVerticalScrollBar().addMouseListener(new MouseAdapter() {
//			public void mouseEntered(MouseEvent e) {
//				context.getVerticalScrollBar().setCursor(Cursor.getDefaultCursor());
//			}
//		});

//		.p(new Combo(0, KeyEvent.VK_PAGE_DOWN), mkAction(b -> b.page(1)).setCaption("Scroll Up"))
//		.p(new Combo(0, KeyEvent.VK_PAGE_UP), mkAction(b -> b.page(-1)).setCaption("Scroll Up"));

//		public void page(int sign) {
//			JScrollBar vertical = getVerticalScrollBar();
//			vertical.setValue(limit(vertical.getValue() + sign * Staff.SISDISPLACE * getModelParent().getSettings().getStepHeight(), 0, vertical.getMaximum()));
//			repaint();
//		}

	// removing stupid built-ins
//		InputMap im = context.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//		im.put(KeyStroke.getKeyStroke("UP"), "none");
//		im.put(KeyStroke.getKeyStroke("DOWN"), "none");
//		im.put(KeyStroke.getKeyStroke("PAGE_UP"), "none");
//		im.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "none");

	private static ContextAction<SheetMusicPanel> mkAction(Consumer<SheetMusicPanel> lambda) {
		ContextAction<SheetMusicPanel> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<SheetMusicPanel> mkFailableAction(Function<SheetMusicPanel, Explain> lambda) {
		ContextAction<SheetMusicPanel> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
