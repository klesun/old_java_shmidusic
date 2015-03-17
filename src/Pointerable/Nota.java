package Pointerable;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import Gui.Constants;
import Gui.DrawPanel;
import Musica.NotnyStan;

final public class Nota extends Pointerable implements IAccord { // TODO: this temporary interface was invented to ease moving Accord storage from Nota
	public int channel = 0;
	public boolean userDefinedChannel = false;
	
	// TODO: store time Nota was pressed and released into file maybe? Just becuse we can!
	public static int time = 0;
	
	public int length = 1;
	
	double autoDur = .25;
	boolean userDurDef = false;
	
	public Nota accord;
	
	public int tune;
	public int forca;
	public int keydownTimestamp;
	
	public int pos;

	// deprecated
	public boolean isTriol = false;
    public String slog = "";
	
	public Nota(int tune){
	    this.tune = tune;             
	    setTune(tune);
	    forca = 127;
	    slog = "";
	    if (!bufInited) {
	    	bufInit();
	    	bufInited = true;        	
	    }
	}
	
	public Nota(int tune, int cislic, int channel) {
		this(tune);
		this.numerator = cislic;
		setChannel(channel);
	}
	
	public Nota(int tune, long elapsed){  	    	
	    this(tune);
	    time += elapsed;
		keydownTimestamp = time;
	}
	
	public Nota(int tune, int forca, int cislic, int autoDur){
		this(tune);
		
	    this.numerator = cislic;
	    this.autoDur = autoDur / 1000.0;
	            
	}
	
	private static String normalizeString(String str, int desiredLength) {
		return String.format("%1$-" + desiredLength + "s", str);
	}
	
	public String getInfoString() {
		return	normalizeString(strTune(this.getAcademicIndex()) + (isBemol() ? "-бемоль" : ""),12) +
				normalizeString(getOctava() + " " + oktIdxToString(getOctava()), 19) +
				normalizeString(channel+"", 2);
	}
	
	@Override
	public String toString() {
		String result = "аккорд:\n";
		Nota curNota = this;
		while (curNota != null) {
			result += "\t" + curNota.getInfoString() + "\n";
			curNota = curNota.accord; } 
	    String s = "nota: "+tune+"; pos: "+getAcademicIndex()+"; okt: "+getOctava()+"; "+strTune(pos);
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

	@Override
	public void setNext( Pointerable elem ) {
	    if (isTriol) {
	        next.next.next = elem;
	    } else next = elem;
	}
	
	@Override
	public void changeDur(int n, boolean single){
		userDurDef = true;
	    if (isTriol) {
	        Nota tmp = this;
	        for (int i=0;i<2;++i) {
	            tmp.next.changeDur(n, false);
	            tmp = (Nota)(tmp.next);
	        }
	    }
		if ( (accord != null) && (single == false) ) accord.changeDur(n, false); 
		
		if (numerator == NotnyStan.DEFAULT_ZNAM*2) {
			numerator = NotnyStan.DEFAULT_ZNAM;
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
	 
	public int getAccLen(){
		if (accord != null) return Math.min(numerator, accord.getAccLen());
		else return numerator;
	}
	
	private static Boolean bufInited = false;
	public static BufferedImage notaImg0[] = new BufferedImage[8];
	public static BufferedImage notaImg[] = new BufferedImage[8];
	public static BufferedImage notaImgCol[] = new BufferedImage[8];
	public static BufferedImage[][] coloredNotas = new BufferedImage[10][8];

	static void bufInit() { // функция запускается только при создании первого экземпляра класса
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
	    
	    refreshSizes();
	
	}
	public static void refreshSizes() {
	    int w1, h1; Graphics2D g;
	    w1 = DrawPanel.notaWidth; h1 = DrawPanel.notaHeight;
	    for (int idx = 0; idx < 8; ++idx ) {
	        notaImg[idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	        g = notaImg[idx].createGraphics();
	        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1.0f));
	        Image scaledImage = null;
	        if (notaImg0[idx]!=null)
	            scaledImage = notaImg0[idx].getScaledInstance(w1, h1, Image.SCALE_SMOOTH);
	        g.drawImage(scaledImage, 0, 0, w1, h1, null);
	        g.dispose();
	
	        notaImgCol[idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	        g = notaImgCol[idx].createGraphics();
	        g.setColor(new Color(127,255,0));
	        g.fillRect(0, 0, DrawPanel.notaWidth, DrawPanel.notaHeight);
	        g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
	        g.drawImage(notaImg[idx], 0, 0, w1, h1, null);
	        g.dispose();                       
	    }
	    
	    for (int chan = 0; chan < 10; ++chan) {        	
	        for (int idx = 0; idx < 8; ++idx) {
	        	coloredNotas[chan][idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	            g = coloredNotas[chan][idx].createGraphics();
	            g.setColor(calcColor(chan));
	            g.fillRect(0, 0, DrawPanel.notaWidth, DrawPanel.notaHeight);
	            g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
	            g.drawImage(notaImg[idx], 0, 0, w1, h1, null);
	            g.dispose();
	        }
	    }
	}
	
	public BufferedImage getImage() {
		int idx = (int)(Math.ceil(7 - Math.log(numerator) / Math.log(2) ));
		return channel > -1? coloredNotas[channel][idx]: notaImg[idx];
	}
	
	public BufferedImage getImageColor() {
	    int idx = (int)(Math.ceil(7 - Math.log(numerator) / Math.log(2) ));
	    return notaImgCol[idx];
	}

	// getters/setters
	
	// implements(Pointerable)
	public LinkedHashMap<String, Object> getExternalRepresentationSuccessed() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<String, Object>();
		dict.put("tune", this.tune);
		dict.put("numerator", this.numerator);
		dict.put("channel", this.channel);
	
		return dict;
	}

	// implements(Pointerable)
	public Nota reconstructFromJson(JSONObject jsObject) throws JSONException {
		this.tune = jsObject.getInt("tune");
		this.numerator = jsObject.getInt("numerator");
		this.channel = jsObject.getInt("channel");
	
		return this;
	}

	// implements(Pointerable)
	public int getWidth() {
		int width = (int)Math.ceil( slog.length() * Constants.FONT_WIDTH / (Constants.STEP_H * 2) );
		if (width < 1) width = 1;

		return width;
	}

	public int getNumerator() {
		return this.numerator;
	}

	public int getDenominator() {
		// TODO: for tuplets
		return 1;
	}	

	public void setChannel(int channel) {
		this.channel = channel;
	}

	private void setTune(int value){		
		this.tune = value;
	}

	public int getOctava() {
		return this.tune/12;
	}

	public int getAcademicIndex() {
		return Nota.tuneToAcademicIndex(this.tune);
	}

	public Boolean isBemol() {
		// 0 - до, 2 - ре, 4 - ми, 5 - фа, 7 - соль, 9 - ля, 10 - си
		int[] bemolTuneList = new int[]{1,3,6,8,10};
	    return Arrays.asList(bemolTuneList).contains(this.tune % 12);
	}

	// private methods

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

	// deprecated
	
	// implements(IAccord)
	public String getSlog() {
		return this.slog;
	}

	// implements(IAccord)
	public IAccord setSlog(String value) {
		this.slog = value;
		return this;
	}

	// implement(IAccord)
	public Nota add(Nota newbie) {
		Nota cur = this;
		Nota rez = cur;		
	    do {
	    	if (cur.tune == newbie.tune) return this; 
	    	if (cur.tune < newbie.tune) {
	    		int tmp = cur.tune;
	    		cur.setTune(newbie.tune);
	    		newbie.setTune(tmp);
	    	}
	    	rez = cur;
	    	cur = cur.accord;
	    } while (cur != null);
		rez.accord = newbie;
		return this;
	}

	// implements(IAccord)
	public Nota getEarliest() {
		return this;
	}
	// implements(IAccord)
	public ArrayList<Nota> getNotaList() {
		ArrayList<Nota> list = new ArrayList<Nota>();
		Nota tmp = this;
		while (tmp != null) {
			list.add(tmp);
			tmp = tmp.accord;
		}
		return list;
	}
}