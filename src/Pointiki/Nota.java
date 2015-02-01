package Pointiki;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import GraphTmp.DrawPanel;

final public class Nota extends Pointerable {
	public int channel = 0;
	public boolean userDefinedChannel = false;
	
	public static int time = 0;
	public int myTime;
	
	public int length = 1;

	double autoDur = .25;
	boolean userDurDef = false;
	
	public Nota accord;
	public boolean isFirst;
	
    public int tune;    
    public int forca;    
    
	public int pos;
    public int okt;
    
    public int tessi = 0;
    
    boolean mergeNext = false;
    public boolean isTriolChild = false;

	public boolean isTriol = false;
    
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
		this.cislic = cislic;
		setChannel(channel);
	}
    
    public Nota(int tune, long elapsed){  	    	
        this(tune);
        time += elapsed;
    	myTime = time;
    }
    
    public Nota(int tune, int forca, int cislic, int autoDur){
    	this(tune);
    	
        this.cislic = cislic;
        this.autoDur = autoDur / 1000.0;
                
    }
    
    public boolean isBemol;

    static int rawToAcad(int midin){
        midin %= 12;
        switch(midin){
            case 11: return 6;  // си
            case 10: return 6;
            case 9: return 5; // ля
            case 8: return 5;
            case 7: return 4; // соль
            case 6: return 4;
            case 5: return 3; // фа
            case 4: return 2; // ми
            case 3: return 2;
            case 2: return 1; // ре
            case 1: return 1;
            case 0: return 0; // до
            default: return -1;
        }
    }
    static int acadToRaw(int pos){
        switch(pos){
            case 6: return 11;  // си
            case 5: return 9;
            case 4: return 7;
            case 3: return 5;
            case 2: return 4;
            case 1: return 2;
            case 0: return 0; // до
            default: return -1;
        }
    }

	private static String normalizeString(String str, int desiredLength) {
		return String.format("%1$-" + desiredLength + "s", str);
	}

	public String getInfoString() {
		return	normalizeString(strTune(this.pos) + (isBemol ? "-бемоль" : ""),12) +
				normalizeString(okt + " " + oktIdxToString(okt), 19) +
				normalizeString(channel+"", 2);
	}

    @Override
    public String toString() {
		String result = "аккорд:\n";
		Nota curNota = this;
		while (curNota != null) {
			result += "\t" + curNota.getInfoString() + "\n";
			curNota = curNota.accord; } 
        String s = "nota: "+tune+"; pos: "+pos+"; okt: "+okt+"; "+strTune(pos);
        return result;
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

    private String strTune(int n){
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
    public Pointerable getNext() {
        if (isTriol) return next.next.next;
        else return next;
    }
    @Override
    public void setNext( Pointerable elem ) {
        if (isTriol) {
            next.next.next = elem;
        } else next = elem;
    }

	private void setTune(int tu){		
		tune = tu;
		okt = tune/12;
        int tmp = tune;        
        tmp %= 12;
        isBemol = false;
        switch(tmp){
            case 11: pos = 6; break; // си
            case 10: pos = 6; isBemol=true; break;
            case 9: pos = 5; break;
            case 8: pos = 5; isBemol=true; break;
            case 7: pos = 4; break;
            case 6: pos = 4; isBemol=true; break;
            case 5: pos = 3; break;
            case 4: pos = 2; break;
            case 3: pos = 2; isBemol=true; break;
            case 2: pos = 1; break;
            case 1: pos = 1; isBemol=true; break;
            case 0: pos = 0; break; // до
            default: pos = -1; break;
        }
	}
    
	public Nota append(Nota newbie){
		Nota cur = this;
		Nota rez = cur;		
        do {
        	if (cur.tune == newbie.tune) return null; 
        	if (cur.tune < newbie.tune) {
        		int tmp = cur.tune;
        		cur.setTune(newbie.tune);
        		newbie.setTune(tmp);
        	}
        	rez = cur;
        	cur = cur.accord;
        } while (cur != null);
		rez.accord = newbie;
		return newbie;
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
		
		if (cislic == znamen*2) {
			cislic = znamen;
			n = 0;
		}
		if (cislic < 4) {
			cislic = 8;
			n = 0;
		}
		while (n > 0){ 
			if (cislic % 3 == 0) {				
				cislic += cislic/3;
			} else {
				cislic += cislic/2;
			}
			--n;
		}
		while (n < 0){
			if (cislic % 3 == 0) {				
				cislic -= cislic/3;
			} else {
				cislic -= cislic/4;
			}
			++n;
		}
	}
    
    
    
    public int getAccLen(){
    	if (accord != null) return Math.min(cislic, accord.getAccLen());
    	else return cislic;
    }

    private static Boolean bufInited = false;
    public static BufferedImage notaImg0[] = new BufferedImage[8];
    public static BufferedImage notaImg[] = new BufferedImage[8];
    public static BufferedImage notaImgCol[] = new BufferedImage[8];
    public static BufferedImage[][] voicedNotas = new BufferedImage[10][8];
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
        
        for (int i = 0; i < 10; ++i) voicedNotas[i] = new BufferedImage[8];        
        
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
            	voicedNotas[chan][idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
	            g = voicedNotas[chan][idx].createGraphics();
	            g.setColor(calcCol(chan));
	            g.fillRect(0, 0, DrawPanel.notaWidth, DrawPanel.notaHeight);
	            g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0f));
	            g.drawImage(notaImg[idx], 0, 0, w1, h1, null);
	            g.dispose();
            }
        }
    }
    
    private static Color calcCol(int n) {
    	return	n == 0 ? new Color(0,0,0) : // black
				n == 1 ? new Color(255,0,0) : // red
				n == 2 ? new Color(0,192,0) : // green
				n == 3 ? new Color(0,0,255) : // blue
				n == 4 ? new Color(255,128,0) : // orange
				n == 5 ? new Color(192,0,192) : // magenta
				n == 6 ? new Color(0,192,192) : // cyan
				Color.GRAY;
    }

    public BufferedImage getImage() {
    	int idx = (int)(Math.ceil(7 - Math.log(cislic) / Math.log(2) ));
    	return channel > -1? voicedNotas[channel][idx]: notaImg[idx];
    }
    public BufferedImage getImageCol() {
        int idx = (int)(Math.ceil(7 - Math.log(cislic) / Math.log(2) ));
        return notaImgCol[idx];
    }
    
    
    private static int pow(int n, int k){
    	if (k == 5) return 16;
    	if (k < 0) return 0;
    	if (k==0) return 1;
    	return n*pow(n, k-1);
    }
    
    double STEP_H = 20.0;
    int FONT_WIDTH = 7; // для Font.MONOSPACED
    public void setSlog( String slog ) {
    	this.slog = slog;
    	gsize = (int)Math.ceil(  slog.length()*FONT_WIDTH / (STEP_H*2)  );
    	if (gsize < 1) gsize = 1;
    }
    
    public int getNoteCountInAccord() { // TODO: щитай их при добавлении нот и храни как переменную
    	Nota tmp = this;		// Да пошёл ты
    	int count = 1;
    	while ((tmp = tmp.accord) != null) {
    		++count;
    	}
    	return count;
    }
    
    public ArrayList<Nota> getAccordList() {
    	ArrayList<Nota> list = new ArrayList<Nota>();
    	Nota tmp = this;
    	while (this != null) {
    		list.add(tmp);
    		tmp = tmp.accord;
    	}
    	return list;
    }

	public void setChannel(int channel) {
		this.channel = channel;
	}
}