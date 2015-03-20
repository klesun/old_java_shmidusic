package Tools;

import Gui.SheetMusic;
import Musica.Staff;
import Pointerable.IAccord;
import Pointerable.Nota;
import Pointerable.Phantom;
import Pointerable.Pointer;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Dictionary;

public class FileProcessor {
	final static byte NEWACCORD = 0;
	final static byte EOS = 1; // End Of String
	final static byte LYRICS = 2;
	final static byte VERSION = 3;
	final static byte VERSION_BEFORE_VERSIONING = 0;
	final static byte VERSION_32_FIRST = 32;
	final static byte VERSION_33_CHANNELS = 33;
	final static byte CURRENT_VERSION = VERSION_33_CHANNELS;
	final static byte PHANT = 4;
	final static byte FLAGS = 16;
	final static int MAXSLOG = 255;
	final static int MINTUNE = 32;
	static File ourFile = null;
	static Staff stan = null; // OOOOOOOOO, гузно себе статичным сделай извращенец
	
	public static void init(Staff stanNew) {
	    stan = stanNew;
	}
	
	public static void savePNG ( File f ) {
	    SheetMusic albert = stan.drawPanel;
	    if (albert == null) out("Что ты пытаешься сохранить, мудак?!");
	    BufferedImage img = new BufferedImage(albert.getWidth(),albert.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    Graphics g = img.createGraphics();
	    g.setColor(Color.GREEN);
	    g.fillRect(15,15,80,80);
	    albert.paintComponent(g);
	
	    try {
	        ImageIO.write(img, "png", f);
	    } catch (IOException e) {
	        out("Ошибка рисования");
	    }
	}
	
	public static void saveMID ( File f ) {
		
	}
	
	public static void savePDF ( File f ) {
	    // TODO: todo
	}
	
	private static void out(String str) {
	    System.out.println(str);
	}

	public static int saveJsonFile( File f, Staff stan ) {
		try {
			JSONObject js = new JSONObject("{}");
			js.put("stanExternalRepresentation", stan.getExternalRepresentation());
			try {
				PrintWriter out = new PrintWriter(f);
				out.println(js.toString(2));
				out.close(); 
			} catch (IOException exc) { System.out.println("У нас тут жорпа с файлом " ); exc.printStackTrace(); }
		} catch (JSONException exc) { System.out.println("У нас тут жорпа с жсоном " ); exc.printStackTrace(); }
		return 0;
	}

	public static int openJsonFile( File f, Staff stan ) {
		try {
			byte[] encoded = Files.readAllBytes(f.toPath());
			String js = new String(encoded, StandardCharsets.UTF_8);
			try {
				JSONObject jsObject = new JSONObject(js);
				stan.reconstructFromJson(jsObject.getJSONObject("stanExternalRepresentation"));
			} catch (Exception exc) { System.out.println("У нас тут жорпа с открытием жсона " ); exc.printStackTrace(); }
		} catch (IOException exc) { System.out.println("Жопа при открытии жс файла"); exc.printStackTrace(); }
		return 0;
	}
}
