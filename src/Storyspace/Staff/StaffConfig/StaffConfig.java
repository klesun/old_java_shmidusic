package Storyspace.Staff.StaffConfig;

import Gui.ImageStorage;
import Gui.Settings;
import Model.AbstractHandler;
import Model.AbstractModel;
import Model.Field.Arr;
import Model.Field.Field;
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

import org.json.JSONException;
import org.json.JSONObject;

public class StaffConfig extends MidianaComponent {

	final private static int MIN_TEMPO = 15; // i doubt you would need longer. with this 1/16 lasts one second
	final private static int MAX_TEMPO = 480; // i hope no one needs more

	final private static int MAX_TACT_NUMERATOR = 32; // four whole notas

	// tempo 60 => quarter nota = 1 second

	// TODO: use Fraction
	private Field<Integer> numerator = new Field<>("numerator", 8, this, n -> limit(n, 1, MAX_TACT_NUMERATOR)); // h.addField("numerator", 8); // because 8x8 = 64; 64/64 = 1; obvious
	private Field<Integer> tempo = new Field<>("tempo", 120, this, n -> limit(n, MIN_TEMPO, MAX_TEMPO)); // h.addField("tempo", 120);

	private Arr<Channel> channelList = new Arr<>("channelList", makeChannelList(), this, Channel.class);

	private List<Channel> makeChannelList() {
		List<Channel> list = new ArrayList<>();

		int[] tones = {0, 65, 66, 43, 19, 52, 6, 91, 9, 14, 0, 0, 0, 0, 0, 0};

		for (int i = 0; i < Channel.CHANNEL_COUNT; ++i) {
			Channel channel = new Channel(this).setInstrument(tones[i]);
			list.add(channel);
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
		/** @legacy we used to have 10 channels instead of 16 in past - so we got it in old files */
		if (channelList.get().size() < Channel.CHANNEL_COUNT) {
			List<Channel> defaultChannelList = makeChannelList();
			for (int i = channelList.get().size(); i < Channel.CHANNEL_COUNT; ++i) {
				channelList.get().add(defaultChannelList.get(i));
			}
		}
		syncSyntChannels();
		return this;
	}

	public void syncSyntChannels() {
		ShortMessage instrMess = new ShortMessage();
		try {
			for (int i = 0; i < getChannelList().size(); ++i) {
				instrMess.setMessage(ShortMessage.PROGRAM_CHANGE, i, this.getChannelList().get(i).getInstrument(), 0);
				DeviceEbun.getPlaybackReceiver().send(instrMess, -1);
			}
		} catch (InvalidMidiDataException exc) { System.out.println("Midi error, could not sync channel instruments!"); }
	}

	public static void syncSyntChannels(AbstractModel c) { ((StaffConfig)c).syncSyntChannels(); }

	public void drawOn(Graphics g, int xIndent, int yIndent, Boolean completeRepaint) {
		int dX = dx()/5, dY = getSettings().getNotaHeight() * 2;
		g.drawImage(this.getImage(), xIndent - dX, yIndent - dY, null);
	}

	public BufferedImage getImage() {
		int w = dx() * 5;
		int h = getSettings().getNotaHeight() * 6;
		BufferedImage rez = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = rez.getGraphics();
		g.setColor(Color.black);

		int tz=8, tc = getNumerator();
		while (tz>4 && tc%2==0) {
			tz /= 2;
			tc /= 2;
		}
		int inches = getSettings().getNotaHeight()*5/8, taktX= 0, taktY = getSettings().getNotaHeight()*2; // 25, 80
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, inches)); // 12 - 7px width
		g.drawString(tc+"", 0 + taktX, inches*4/5 + taktY);
		int delta = 0 + (tc>9 && tz<10? inches*7/12/2: 0) + ( tc>99 && tz<100?inches*7/12/2:0 );
		g.drawString(tz+"", delta + taktX, 2*inches*4/5 + taktY);

		int tpx = 0, tpy = 0;
		g.drawImage(getImageStorage().getQuarterImage(), tpx, tpy, null);
		inches = getSettings().getNotaHeight() * 9/20;
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, inches)); // 12 - 7px width
		g.drawString(" = " + getTempo() , tpx + dx() * 4/5, tpy + inches*4/5 + getSettings().getNotaHeight()*13/20);

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

	public Integer getTempo() { return this.tempo.get(); }
	public StaffConfig setTempo(int value) { this.tempo.set(value); return this; }
	public Integer getNumerator() { return this.numerator.get(); }
	public StaffConfig setNumerator(int value) { this.numerator.set(value); return this; }
	public List<Channel> getChannelList() { return (ArrayList<Channel>)this.channelList.get(); }

	public int getVolume(int channel) {
		Channel chan = getChannelList().get(channel);
		return chan.getIsMuted() ? 0 : chan.getVolume();
	}
}
