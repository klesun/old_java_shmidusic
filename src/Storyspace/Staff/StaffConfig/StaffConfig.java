package Storyspace.Staff.StaffConfig;

import Gui.ImageStorage;
import Gui.Settings;
import Model.AbstractHandler;
import Model.AbstractModel;
import Model.Field.Arr;
import Model.Field.ModelField;
import Storyspace.Staff.MidianaComponent;
import Stuff.Midi.DeviceEbun;
import Storyspace.Staff.Staff;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.apache.commons.math3.fraction.Fraction;
import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

public class StaffConfig extends MidianaComponent {

	// TODO: use Fraction
	private ModelField<Integer> numerator = h.addField("numerator", 8); // because 8x8 = 64; 64/64 = 1; obvious
	private ModelField<Integer> tempo = h.addField("tempo", 120);

	private Arr<Channel> channelList = (Arr<Channel>)h.addField("channelList", makeChannelList(), Channel.class);

	private List<Channel> makeChannelList() {
		List<Channel> list = new ArrayList<>();

		int[] tones = {0, 65, 66, 43, 19, 52, 6, 91, 9, 14};
		int[] volumes = {60, 60, 60, 60, 60, 60, 60, 60, 60, 60};
		Boolean[] mutes = {false, false, false, false, false, false, false, false, false, false};

		for (int i = 0; i < 10; ++i) {
			list.add(new Channel(this).setInstrument(tones[i]).setVolume(volumes[i]).setIsMuted(mutes[i]));
		}

		return list;
	}

	public StaffConfig(Staff staff) {
		super(staff);
	}

	public ConfigDialog getDialog() { return new ConfigDialog(this); }

	public Fraction getTactSize() {
		int tactNumerator = getNumerator() * 8;
		int tactDenominator = Staff.DEFAULT_ZNAM;
		return new Fraction(tactNumerator, tactDenominator);
	}

	@Override
	public StaffConfig reconstructFromJson(JSONObject jsObject) throws JSONException {
		super.reconstructFromJson(jsObject);
		syncSyntChannels();
		return this;
	}

	public void syncSyntChannels() {
		ShortMessage instrMess = new ShortMessage();
		try {
			for (int i = 0; i < getChannelList().size(); ++i) {
				instrMess.setMessage(ShortMessage.PROGRAM_CHANGE, i, this.getChannelList().get(i).getInstrument(), 0);
				DeviceEbun.theirReceiver.send(instrMess, -1);
			}
		} catch (InvalidMidiDataException exc) { System.out.println("Midi error, could not sync channel instruments!"); }
	}

	public static void syncSyntChannels(AbstractModel c) { ((StaffConfig)c).syncSyntChannels(); }

	@Override
	public void drawOn(Graphics g, int xIndent, int yIndent) {
		int dX = Settings.getNotaWidth()/5, dY = Settings.getNotaHeight()*2;
		g.drawImage(this.getImage(), xIndent - dX, yIndent - dY, null);
	}

	public BufferedImage getImage() {
		int w = Settings.getNotaWidth() * 5;
		int h = Settings.getNotaHeight() * 6;
		BufferedImage rez = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = rez.getGraphics();
		g.setColor(Color.black);

		int tz=8, tc = getNumerator();
		while (tz>4 && tc%2==0) {
			tz /= 2;
			tc /= 2;
		}
		int inches = Settings.getNotaHeight()*5/8, taktX= 0, taktY=Settings.getNotaHeight()*2; // 25, 80
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, inches)); // 12 - 7px width
		g.drawString(tc+"", 0 + taktX, inches*4/5 + taktY);
		int delta = 0 + (tc>9 && tz<10? inches*7/12/2: 0) + ( tc>99 && tz<100?inches*7/12/2:0 );
		g.drawString(tz+"", delta + taktX, 2*inches*4/5 + taktY);

		int tpx = 0, tpy = 0;
		g.drawImage(ImageStorage.inst().getQuarterImage(), tpx, tpy, null);
		inches = Settings.getNotaHeight()*9/20;
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, inches)); // 12 - 7px width
		g.drawString(" = " + getTempo() , tpx + Settings.getNotaWidth()*4/5, tpy + inches*4/5 + Settings.getNotaHeight()*13/20);

		return rez;
	}

	@Override
	public MidianaComponent getFocusedChild() {
		return null;
	}
	@Override
	protected AbstractHandler makeHandler() { return new AbstractHandler(this) {}; }

	// field getters
	
	public Staff getParentStaff() { return (Staff)this.getModelParent(); }

	public Integer getTempo() { return this.tempo.getValue(); }
	public StaffConfig setTempo(int value) { this.tempo.setValue(value); return this; }
	public Integer getNumerator() { return this.numerator.getValue(); }
	public StaffConfig setNumerator(int value) { this.numerator.setValue(value); return this; }
	public List<Channel> getChannelList() { return this.channelList.getValue(); }

	public int getVolume(int channel) {
		Channel chan = getChannelList().get(channel);
		return chan.getIsMuted() ? 0 : chan.getVolume();
	}
}
