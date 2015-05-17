package Stuff.Tools;

import Model.Helper;
import Main.Main;
import Storyspace.Staff.Staff;
import Storyspace.Storyspace;
import Model.IModel;

import javax.imageio.ImageIO;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileProcessor {
	
	public static void savePNG ( File f, Staff staff ) {
	    BufferedImage img = new BufferedImage(staff.getWidth(), staff.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    Graphics g = img.createGraphics();
	    g.setColor(Color.GREEN);
	    g.fillRect(15, 15, 80, 80);
		staff.drawOn(g, 0, 0);
	
	    try {
	        ImageIO.write(img, "png", f);
	    } catch (IOException e) {
			System.out.println("Ошибка рисования");
	    }
	}

	public static void saveStoryspace(File f, Storyspace storyspace) {
		saveModel(f, storyspace);
		Main.window.setTitle(f.getAbsolutePath());
	}

	public static void saveMusicPanel(File f, Staff staff) {
		saveModel(f, staff);
		if (staff.getParentSheet().getStoryspaceScroll() != null) {
			staff.getParentSheet().getStoryspaceScroll().setTitle(f.getName());
		}
	}

	public static void openStoryspace(File f, Storyspace storyspace) {
		openModel(f, storyspace, "");
		Main.window.setTitle(f.getAbsolutePath());
	}

	public static void openStaff(File f, Staff staff) {
		openModel(f, staff, "stanExternalRepresentation");
		if (staff.getParentSheet().getStoryspaceScroll() != null) {
			staff.getParentSheet().getStoryspaceScroll().setTitle(f.getName());
		}
	}

	private static void openModel(File f, IModel model, String legacy) {
		try {
			byte[] encoded = Files.readAllBytes(f.toPath());
			String js = new String(encoded, StandardCharsets.UTF_8);
			try {
				JSONObject jsObject = new JSONObject(js);
				if (js.contains(model.getClass().getSimpleName())) {
					model.reconstructFromJson(jsObject.getJSONObject(model.getClass().getSimpleName()));
				} else { // legacy
					model.reconstructFromJson(jsObject.getJSONObject(legacy));
				}
			} catch (Exception exc) { System.out.println("У нас тут жорпа с открытием жсона " ); exc.printStackTrace(); }
		} catch (IOException exc) { System.out.println("Жопа при открытии жс файла"); exc.printStackTrace(); }
	}

	private static void saveModel(File f, IModel model) {
		try {
			JSONObject js = new JSONObject("{}");
			js.put(model.getClass().getSimpleName(), Helper.getJsonRepresentation(model));
			try {
				PrintWriter out = new PrintWriter(f);
				out.println(js.toString(2));
				out.close();
			} catch (IOException exc) { System.out.println("У нас тут жорпа с файлом " ); exc.printStackTrace(); }
		} catch (JSONException exc) { System.out.println("У нас тут жорпа с жсоном " ); exc.printStackTrace(); }
	}
}
