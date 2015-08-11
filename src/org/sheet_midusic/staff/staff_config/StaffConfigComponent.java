package org.sheet_midusic.staff.staff_config;

import org.klesun_model.AbstractHandler;
import org.klesun_model.Combo;
import org.klesun_model.ContextAction;
import org.sheet_midusic.staff.MidianaComponent;
import org.sheet_midusic.staff.staff_panel.StaffComponent;
import org.sheet_midusic.stuff.OverridingDefaultClasses.TruMap;
import org.sheet_midusic.stuff.graphics.ImageStorage;
import org.sheet_midusic.stuff.graphics.Settings;
import org.sheet_midusic.stuff.graphics.ShapeProvider;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class StaffConfigComponent extends MidianaComponent
{
	final private StaffConfig staffConfig;

	public StaffConfigComponent(StaffComponent parent, StaffConfig staffConfig) {
		super(parent);  /** @deprecated */
		this.staffConfig = staffConfig;
	}

	@Override
	public MidianaComponent getFocusedChild() { return null; }

	@Override
	protected AbstractHandler makeHandler() {
		return new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
				return new TruMap<>();
			}
		};
	}

	public int drawOn(Graphics2D g, int x, int y) {
		int dX = dx()/5, dY = Settings.inst().getNotaHeight() * 2;
		drawImage(g, x - dX, y - dY);

		drawSignature(g, x + dX / 4, y);

		return -100;
	}

	private void drawSignature(Graphics2D g, int x, int y)
	{
		KeySignature siga = new KeySignature(staffConfig.keySignature.get());

		ShapeProvider shaper = new ShapeProvider(Settings.inst(), g, ImageStorage.inst());

		BiConsumer<Integer, Integer> paintEbony = staffConfig.keySignature.get() > 0
				? shaper::drawSharpSign
				: shaper::drawFlatSign;

		int doPositionY = y + 10 * dy(); // y is toppest Staff line
		int i = 0;
		for (int ivory: siga.getAffectedIvorySet()) {
			int positionY = doPositionY - ivory * dy();

			if (ivory < KeySignature.SO) {
				positionY -= 7 * dy();
			}

			// dealing with them covering one another
			int xShift = i * dx() / 2;
			if (i > 3) { xShift -= dx(); }

			paintEbony.accept(x + xShift, positionY);

			++i;
		}
	}

	public void drawImage(Graphics2D g, int x, int y)
	{
		g.setColor(Color.black);
		int inches = Settings.inst().getNotaHeight()*5/8, taktY = Settings.inst().getNotaHeight()*2; // 25, 80
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, inches)); // 12 - 7px width

		int tz = 8, tc = staffConfig.getNumerator(); // tz - denominator, tc - numerator
		while (tz>4 && tc%2==0) {
			tz /= 2;
			tc /= 2;
		}

		g.drawString(tc+"", x - dx() / 2, y + inches*4/5 + taktY);
		g.drawString(tz+"", x - dx() / 2, y + 2 * inches*4/5 + taktY);

		int tpx = x, tpy = y + dy() * 2;
		g.drawImage(ImageStorage.inst().getQuarterImage(), tpx, tpy, null);
		inches = Settings.inst().getNotaHeight() * 9/20;
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, inches)); // 12 - 7px width
		g.drawString(" = " + staffConfig.getTempo(), tpx + dx() * 4 / 5, tpy + inches * 4 / 5 + Settings.inst().getNotaHeight() * 13 / 20);
	}
}
