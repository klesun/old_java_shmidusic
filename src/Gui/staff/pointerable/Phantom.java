package Pointerable;

import Gui.SheetMusic;
import Musica.Staff;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

public class Phantom extends Pointerable {	

    Staff stan;
   
	public int valueTempo = 120;
	public int valueInstrument = 0;
	public double valueVolume = 0.5;
	public int znamen = Staff.DEFAULT_ZNAM;

	public Phantom() {
        znamen = 8;
        numerator = 8;
        underPtr = true;
	}

	@Override
	public BufferedImage getImage() {
		int w = SheetMusic.notaWidth*5;
		int h = SheetMusic.notaHeight*6;
		BufferedImage rez = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = rez.getGraphics();
		g.setColor(Color.black);

		int tz=znamen, tc = numerator;
		while (tz>4 && tc%2==0) {
			tz /= 2;
			tc /= 2;
		}
		int inches = SheetMusic.notaHeight*5/8, taktX= 0, taktY=SheetMusic.notaHeight*2; // 25, 80
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, inches)); // 12 - 7px width
		g.drawString(tc+"", 0 + taktX, inches*4/5 + taktY);
		int delta = 0 + (tc>9 && tz<10? inches*7/12/2: 0) + ( tc>99 && tz<100?inches*7/12/2:0 );
		g.drawString(tz+"", delta + taktX, 2*inches*4/5 + taktY);

		int tpx = 0, tpy = 0;
		g.drawImage(Nota.notaImg[3], tpx, tpy, null);
		inches = SheetMusic.notaHeight*9/20;
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, inches)); // 12 - 7px width
		g.drawString(" = "+valueTempo, tpx + SheetMusic.notaWidth*4/5, tpy + inches*4/5 + SheetMusic.notaHeight*13/20);

		// instrument
		tpy = SheetMusic.notaHeight;
		g.drawImage(SheetMusic.vseKartinki[5], 0, tpy, null);
		g.setFont(new Font(Font.SERIF, Font.BOLD, inches)); // 12 - 7px width
		g.setColor(Color.decode("0xaa00ff"));
		g.drawString(" "+valueInstrument, 0 + SheetMusic.notaWidth*3/5, tpy + inches*4/5 + SheetMusic.notaHeight*11/20);

		// Volume
		tpx = 0; tpy = SheetMusic.notaHeight*37/10;
		g.drawImage(SheetMusic.vseKartinki[4], tpx+SheetMusic.notaWidth*2/25, tpy, null);
		inches = SheetMusic.notaHeight*3/10;
		g.setColor(Color.decode("0x00A13E"));
		g.drawString((int)(valueVolume*100)+"%", tpx, tpy + inches*4/5 + SheetMusic.notaHeight*2/5);

		return rez;
	}

	@Override
	public void changeDur(int i, boolean b) {}

	@Override
	public LinkedHashMap<String, Object> getExternalRepresentationSuccessed() {
		LinkedHashMap<String, Object> dict = new LinkedHashMap<String, Object>();
		dict.put("tempo", this.valueTempo);
		dict.put("volume", this.valueVolume);
		dict.put("instrument", this.valueInstrument);
		dict.put("denominator", this.znamen);

		return dict;
	}

	@Override
	public Phantom reconstructFromJson(JSONObject jsObject) throws JSONException {
		System.out.println(jsObject);
		this.valueTempo = jsObject.getInt("tempo");
		this.valueVolume = jsObject.getDouble("volume");
		this.valueInstrument = jsObject.getInt("instrument");
		this.znamen = jsObject.getInt("denominator");

		return this;
	}

	public Phantom update(Phantom rival) throws JSONException {
		JSONObject js = new JSONObject("{}"); 
		js = new JSONObject(js.put("lol", rival.getExternalRepresentation()).get("lol").toString());
		this.reconstructFromJson(js);
		return this;
	}

    public enum WhatToChange {
        cislicelj,
        znamenatelj,
        tempo,
        volume,
		instrument;
    }
    public WhatToChange changeMe = WhatToChange.cislicelj;

	public void chooseNextParam() {
        switch (changeMe) {
			case cislicelj:
				changeMe = WhatToChange.tempo;
				break;
			case tempo:
				changeMe = WhatToChange.volume;
				break;
			case volume:
				changeMe = WhatToChange.instrument;
				break;
			case instrument:
				changeMe = WhatToChange.cislicelj;
				break;
			case znamenatelj: // okay...
				changeMe = WhatToChange.cislicelj;
				break;
			default:
				changeMe = WhatToChange.cislicelj;
				System.out.println("Неизвестный енум");
				break;
        } // switch(enum)
	}

    public int tryToWrite( char c ) {
        if (c < '0' || c > '9') return -1;
        switch (changeMe) {
            case tempo:
                valueTempo *= 10;
                valueTempo += c - '0';
                valueTempo %= 12000;
                break;
			case instrument:
                valueInstrument *= 10;
                valueInstrument += c - '0';
                valueInstrument %= 256;
                break;
            case volume:
                valueVolume *= 10;
                System.out.println("c="+c+" c-'0'="+(c-'0'));
                valueVolume += ((double)(c-'0'))/100;
                if (valueVolume > 2.54) valueVolume = 2.54;
                break;
            default:
                System.out.println("Неизвестный енум");
                break;
        } // switch(enum)
        return 0;
    }

    public void changeValue(int n) {
        switch (changeMe) {
            case cislicelj:
                numerator += n;
                numerator = numerator < 1 ? 1 : numerator % 257;
                break;
            case tempo:
                valueTempo += n;
                valueTempo = valueTempo < 1 ? 1 : valueTempo % 12000;
                break;
			case instrument:
                valueInstrument += n;
				valueInstrument = valueInstrument < 0 ? 0 : valueInstrument % 256;
                break;
            case volume:
                valueVolume += ((double)n)/100;
                if (valueVolume < 0) valueVolume = 0;
                if (valueVolume > 2.54) valueVolume = 2.54;
                break;
            default:
                System.out.println("Неизвестный енум");
                break;
        } // switch(enum)
    }

    public void backspace() {
        switch (changeMe) {
			case tempo:
				valueTempo /= 10;
				if (valueTempo < 1) valueTempo = 1;
				break;
			case volume:
				int tmp = (int)(valueVolume*100);
				tmp /= 10;
				valueVolume = ((double)tmp)/100;
				break;
			case instrument:
				valueInstrument /= 10;
				break;
			default:
				System.out.println("Неизвестный енум");
				break;
        } // switch(enum)
    }

    public void setCislicFromFile( int fileCis ) {
        fileCis /= 8;
        numerator = fileCis;
    }

    private double log2(int n){
        return Math.log(n)/Math.log(2);
    }
   
    // getters/setters
    
    // implements(Pointerable)
    public int getWidth() {
    	return 1;
    }
	
}
