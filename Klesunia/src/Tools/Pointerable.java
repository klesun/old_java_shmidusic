package Tools;

import java.awt.image.BufferedImage;

import Musica.Nota;
import Musica.NotnyStan;

public abstract class Pointerable {
	public int durCislic = 16;
	public int durZnamen = NotnyStan.DEFAULT_ZNAM;
	
	public Nota next;
	public Pointerable prev;
	
	abstract public BufferedImage getImage();
    
    public String slog = "";
    
    public Pointerable() {
    }
	
	public static int round(double n) {
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
	
	public abstract void changeDur(int i, boolean b);
}
