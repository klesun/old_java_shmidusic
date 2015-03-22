package Gui.staff.pointerable;

import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import Gui.staff.Staff;

public abstract class Pointerable {

	public boolean underPtr = false;
	
	public Accord next;
	public Pointerable prev;
    public Pointerable retrieve;

    public Accord getNext() { return next; }
    public void setNext( Accord elem ) { next = elem; }
	
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
	public abstract LinkedHashMap<String, Object> getObjectState();
	public abstract Pointerable setObjectStateFromJson(JSONObject jsObj) throws JSONException;
	abstract public BufferedImage getImage();
	public abstract int getWidth(); // TODO: for now it returns not in pixels, but in conventional unit (one Accord without text takes one conventional unit)
}
