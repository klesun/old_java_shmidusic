package Model.Accord.Nota;


import Model.Accord.Accord;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import Gui.Settings;
import Gui.SheetMusic;
import Model.AbstractModel;
import Model.StaffConfig.StaffConfig;
import Model.Staff;
import Tools.Fp;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Graphics;
import java.util.List;

public class Nota extends AbstractModel {
	
// TODO: store time Nota was pressed and released into file maybe? Just becuse we can!
	public static int time = 0;
	
	public int length = 1;	
	
	public int tune;
	public int channel = 0;
	public Boolean isSharp = false;

	public int numerator = 16;
	public int tupletDenominator = 1;

	public int keydownTimestamp;

	public Nota(Accord parent) {
		super(parent);
	}

	private static String normalizeString(String str, int desiredLength) {
		return String.format("%1$-" + desiredLength + "s", str);
	}
	
	public String getInfoString() {
		return	normalizeString(strTune(this.getAcademicIndex()) + (isEbony() ? "-бемоль" : ""),12) +
				normalizeString(getOctava() + " " + oktIdxToString(getOctava()), 19) +
				normalizeString(channel+"", 2);
	}
	
	@Override
	public String toString() {
		String result = "аккорд:\n";
		result += "\t" + this.getInfoString() + "\n";
	    String s = "nota: "+tune+"; pos: "+getAcademicIndex()+"; okt: "+getOctava()+"; "+strTune(this.getAcademicIndex());
	    return result;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tune;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nota other = (Nota) obj;
		if (tune != other.tune)
			return false;
		return true;
	}
	
	// TODO: broken and bad name and gay
	public void changeDur(int n) {

		if (numerator == Staff.DEFAULT_ZNAM*2) {
			numerator = Staff.DEFAULT_ZNAM;
			n = 0;
		}
		if (numerator < 4) {
			numerator = 8;
			n = 0;
		}
		while (n > 0){ 
			if (numerator % 3 == 0) {				
				numerator += numerator/3;
			} else {
				numerator += numerator/2;
			}
			--n;
		}
		while (n < 0){
			if (numerator % 3 == 0) {				
				numerator -= numerator/3;
			} else {
				numerator -= numerator/4;
			}
			++n;
		}
	}

	public Boolean isLongerThan(Nota rival) {
		return this.getNumerator() * rival.getDenominator() > rival.getNumerator() * this.getDenominator(); 
	}
	
	private static Boolean bufInited = false;
	public static BufferedImage notaImg0[] = new BufferedImage[8];
	public static BufferedImage notaImg[] = new BufferedImage[8];
	public static BufferedImage[] notaImageFocused = new BufferedImage[8];
	public static BufferedImage[][] coloredNotas = new BufferedImage[10][8];

	public static void bufInit(SheetMusic sheet) {
		File notRes[] = new File[8];
	    for (int idx = -1; idx<7; ++idx){
	    	String str = "imgs/" + pow(2, idx) + "_sized.png";
	    	notRes[idx+1] = new File(str);
	    }
	    System.out.println("Working Directory = " +
	    System.getProperty("user.dir"));
	    for (int idx = 0; idx < 8; ++idx){
	    	try {
	            notaImg0[idx] = ImageIO.read(notRes[idx]);
	    	} catch (IOException e) { if (idx!=7) System.out.println(e+" Ноты не читаются!!! "+idx+" "+notRes[idx].getAbsolutePath()); }
	    }
	    
	    for (int i = 0; i < 10; ++i) coloredNotas[i] = new BufferedImage[8];        
	    
	    refreshSizes(sheet);
	
	}
	public static void refreshSizes(SheetMusic sheet) {
	    int w1, h1; Graphics2D g;
	    w1 = sheet.getNotaWidth(); h1 = sheet.getNotaHeight();
	    for (int idx = 0; idx < 8; ++idx ) {
	        notaImg[idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	        g = notaImg[idx].createGraphics();
	        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1.0f));
	        Image scaledImage = null;
	        if (notaImg0[idx]!=null)
	            scaledImage = notaImg0[idx].getScaledInstance(w1, h1, Image.SCALE_SMOOTH);
	        g.drawImage(scaledImage, 0, 0, w1, h1, null);
	        g.dispose();
	
	        notaImageFocused[idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	        g = notaImageFocused[idx].createGraphics();
	        g.setColor(new Color(127,255,0));
	        g.fillRect(0, 0, sheet.getNotaWidth(), sheet.getNotaHeight());
	        g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
	        g.drawImage(notaImg[idx], 0, 0, w1, h1, null);
	        g.dispose();                       
	    }
	    
	    for (int chan = 0; chan < 10; ++chan) {        	
	        for (int idx = 0; idx < 8; ++idx) {
	        	coloredNotas[chan][idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	            g = coloredNotas[chan][idx].createGraphics();
	            g.setColor(calcColor(chan));
	            g.fillRect(0, 0, sheet.getNotaWidth(), sheet.getNotaHeight());
	            g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
	            g.drawImage(notaImg[idx], 0, 0, w1, h1, null);
	            g.dispose();
	        }
	    }
	}

	@Override
	public void drawOn(Graphics surface, int x, int y) {
		surface.setColor(Color.BLACK);

		if (this.isEbony()) {
			surface.drawImage(isSharp ? getSheet().getSharpImage() : getSheet().getFlatImage(), x + getSheet().dx() / 2, y + 3 * getSheet().dy() +2, null);
		}

		int idx = (int)(Math.ceil(7 - Math.log(numerator) / Math.log(2) ));
		BufferedImage tmpImg = channel > -1 ? coloredNotas[channel][idx] : notaImg[idx];

		surface.drawImage(tmpImg, x + getNotaImgRelX(), y, null);

		if (this.tupletDenominator != 1) { for (int i = 0; i < 3; ++i) { surface.drawLine(x + getStickX(), y + i, x + getStickX() -6, y + i); } }
		if (this.numerator % 3 == 0) surface.fillOval(x + Settings.inst().getStepWidth() + getWidth()*2/5, y + getHeight()*7/8, getHeight()/8, getHeight()/8);
	}

	// getters/setters
	
	public LinkedHashMap<String, Object> getJsonRepresentation() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<String, Object>();
		dict.put("tune", this.tune);
		dict.put("numerator", this.numerator);
		dict.put("channel", this.channel);
		dict.put("isSharp", this.isSharp);
		dict.put("tupletDenominator", this.tupletDenominator);
	
		return dict;
	}

	public Nota reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.tune = jsObject.getInt("tune");
		this.numerator = jsObject.getInt("numerator");
		this.channel = jsObject.getInt("channel");
		if (jsObject.has("isSharp")) { this.isSharp = jsObject.getBoolean("isSharp"); }
		if (jsObject.has("tupletDenominator")) { this.tupletDenominator = jsObject.getInt("tupletDenominator"); }
	
		return this;
	}

	// implements(Pointerable)
	public int getWidth() {
		// TODO: use it in Accord.getWidth()
//		int width = (int)Math.ceil( slog.length() * Constants.FONT_WIDTH / (Constants.STEP_H * 2) );
//		if (width < 1) width = 1;
		return this.getParentAccord().getParentStaff().parentSheetMusic.getNotaWidth() * 2;
	}

	public int getHeight() {
		return this.getParentAccord().getParentStaff().parentSheetMusic.getNotaHeight();
	}

	public int getOctava() {
		return this.tune/12;
	}

	public int getAcademicIndex() {
		int idx = Nota.tuneToAcademicIndex(this.tune);
		if (isEbony() && isSharp) {
			idx -= 1;
		}
		return idx;
	}

	public Boolean isEbony() {
		// 0 - до, 2 - ре, 4 - ми, 5 - фа, 7 - соль, 9 - ля, 10 - си
		int[] bemolTuneList = new int[]{ 1,3,6,8,10 };
		return inArray(bemolTuneList, this.tune % 12);
	}

	public Boolean isBotommedToFitSystem() { // 8va
		return this.getOctava() > 6;
	}
	
	public int getAbsoluteAcademicIndex() {
		return getAcademicIndex() + getOctava() * 7;
	}
	public Boolean isStriked() {
		return getAbsoluteAcademicIndex() % 2 == 1;
	}

	public List<Integer> getAncorPoint() {
		return Arrays.asList(getWidth()*16/25, Settings.inst().getStepHeight() * 7);
	}

	public List<Integer> getTraitCoordinates() {
		ArrayList result = new ArrayList();
		result.addAll(Fp.vectorSum(getAncorPoint(), Arrays.asList(-getWidth()*6/25, 0)));
		result.addAll(Fp.vectorSum(getAncorPoint(), Arrays.asList(+getWidth()*6/25, 0)));
		return result;
	}

	public int getNotaImgRelX() {
		return this.getWidth() / 2;
	}

	public int getStickX() {
		return this.getNotaImgRelX() + getSheet().dx() / 2;
	}

	public int getTimeMiliseconds() {
		int minute = 60 * 1000;
		StaffConfig config = getParentAccord().getParentStaff().getPhantom();
		return minute * 4 / Staff.DEFAULT_ZNAM / config.valueTempo * getNumerator() / getDenominator();
		// 4 - будем брать четвертную как основную
	}

	public byte getVolume() {
		if (this.tune == 36) {
			return 0; // пауза лол какбэ
		} else {
			// TODO: maybe could make basses louder?
			return (byte)(127 * getParentAccord().getParentStaff().getPhantom().valueVolume);
		}
	}

	// field getters/setters

	public Accord getParentAccord() {
		return (Accord)this.getParent();
	}

	public int getNumerator() {
		return this.numerator;
	}

	public int getDenominator() {
		return 1 * this.getTupletDenominator();
	}

	public int getTupletDenominator() {
		return this.tupletDenominator;
	}

	public Nota setTupletDenominator(int value) {
		this.tupletDenominator = value;
		return this;
	}

	public Nota setChannel(int channel) {
		this.channel = channel;
		return this;
	}

	public Nota setTune(int value){		
		this.tune = value;
		return this;
	}
	
	public Nota setKeydownTimestamp(int value) {
		this.keydownTimestamp = value;
		return this;
	}

	public Nota triggerIsSharp() {
		this.isSharp = !this.isSharp;
		return this;
	}

	// private methods

	private SheetMusic getSheet() {
		return this.getParentAccord().getParentStaff().parentSheetMusic;
	}
			
	// private static methods

	private static int pow(int n, int k){
		if (k == 5) return 16;
		if (k < 0) return 0;
		if (k==0) return 1;
		return n*pow(n, k-1);
	}

	private static Color calcColor(int n) {
		return	n == 0 ? new Color(0,0,0) : // black
				n == 1 ? new Color(255,0,0) : // red
				n == 2 ? new Color(0,192,0) : // green
				n == 3 ? new Color(0,0,255) : // blue
				n == 4 ? new Color(255,128,0) : // orange
				n == 5 ? new Color(192,0,192) : // magenta
				n == 6 ? new Color(0,192,192) : // cyan
				Color.GRAY;
	}

	private static int tuneToAcademicIndex(int tune){
		tune %= 12;
	    switch(tune){

			case 11:case 10:  return 6;  // си, си-бемоль
			case 9:case 8: return 5; // ля, ля-бемоль
			case 7:case 6: return 4; // соль, соль-бемоль
			case 5: return 3; // фа
			case 4:case 3: return 2; // ми
			case 2:case 1: return 1; // ре
			case 0: return 0; // до
			
			default: return -1;
	    }
	}

	private static String oktIdxToString(int idx) {
		return  idx == 1 ?	"субконтроктава" :
				idx == 2 ?	"контроктава" :
				idx == 3 ?	"большая октава" :
				idx == 4 ?	"малая октава" :
				idx == 5 ?	"первая октава" :
				idx == 6 ?	"вторая октава" :
				idx == 7 ?	"третья октава" :
				idx == 8 ?	"четвёртая октава" :
				idx == 9 ?	"пятая октава" :
							"не знаю          ";
	}

	private static String strTune(int n){
	    if (n < 0) {
	        n += Integer.MAX_VALUE - Integer.MAX_VALUE%12;
	    }
	    n %= 12;
	    switch(n){
	        case 0: return "до";
	        case 1: return "ре";
	        case 2: return "ми";
	        case 3: return "фа";
	        case 4: return "соль";
	        case 5: return "ля";
	        case 6: return "си";
	        default: return "ша-бемоль";
	    }
	}

	// retarded language
	private static Boolean inArray(int[] arr, int n) {
		for (int i = 0; i < arr.length; ++i) {
			if (arr[i] == n) return true;
		}
		return false;
	}

	@Override
	public List<? extends AbstractModel> getChildList() {
		return new ArrayList<>();
	}

	@Override
	public AbstractModel getFocusedChild() {
		return null;
	}

	@Override
	protected Boolean undoFinal() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected Boolean redoFinal() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}