package Pointerable;

import java.awt.image.BufferedImage;
import java.util.Dictionary;

import org.json.JSONException;
import org.json.JSONObject;

import Musica.NotnyStan;

public abstract class Pointerable {

	public boolean underPtr = false;
	
	public int numerator = 16; // TODO: it should be Accord (or Nota?) specific
	
	public Pointerable next;
	public Pointerable prev;
    public Pointerable retrieve;

    public Pointerable getNext() { return next; }
    public void setNext( Pointerable elem ) { next = elem; }
    
    public Pointerable() {
    }
	
	public static int round(double n) { // TODO: seems to be unused
    			if (n >= 96 + 16) return 128; // 
    	else	if (n >= 96 - 16) return 96; // 
    	else 	if (n >= 48 + 8) return 64; // 
    	else 	if (n >= 48 - 8) return 48; // 1/2 
    	else	if (n >= 24 + 4) return 32; 
    	else 	if (n >= 24 - 4) return 24; 
    	else 	if (n >= 12 + 2) return 16;
    	else	if (n >= 12 - 2) return 12; 
    	else 	if (n >= 6 + 1) return 8; 
    	else 	if (n >= 6 - 1) return 6;
    	else 					 return 4;
    }

	public String getPointerableClass() {
		return this.getClass().getName();
	}

	public abstract void changeDur(int i, boolean b);
	public abstract Dictionary<String, Object> getExternalRepresentationSuccessed();
	public abstract Pointerable reconstructFromJson(JSONObject jsObj) throws JSONException;
	abstract public BufferedImage getImage();
	public abstract int getWidth(); // TODO: for now it returns not in pixels, but in conventional unit (one Accord without text takes one conventional unit)

	public Dictionary<String, Object> getExternalRepresentation() {
		Dictionary<String, Object> dict = this.getExternalRepresentationSuccessed();
		dict.put("pointerableClass", this.getClass().getSimpleName());

		return dict;
	}

	final public static Pointerable getSuccessor(String className) {
		if (className.equals("Accord")) return new Accord();
		if (className.equals("Phantom")) return new Phantom();
		if (className.equals("Nota")) return new Nota(63); // Deprecated (and not just because we put here random hardcoded tune)

		return null;
	}
}
