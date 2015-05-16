package Stuff.Tools;

import Storyspace.Music.MusicPanel;
import Main.Main;
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
	
	public static void savePNG ( File f, MusicPanel albert ) {
	    BufferedImage img = new BufferedImage(albert.getWidth(),albert.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    Graphics g = img.createGraphics();
	    g.setColor(Color.GREEN);
	    g.fillRect(15, 15, 80, 80);
	    albert.paintComponent(g);
	
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

	public static void saveMusicPanel(File f, MusicPanel musicPanel) {
		saveModel(f, musicPanel.getStaff());
		if (musicPanel.getStoryspaceScroll() != null) {
			musicPanel.getStoryspaceScroll().setTitle(f.getName());
		}
	}

	public static void openStoryspace(File f, Storyspace storyspace) {
		openModel(f, storyspace, "");
		Main.window.setTitle(f.getAbsolutePath());
	}

	public static void openMusicPanel(File f, MusicPanel musicPanel) {
		openModel(f, musicPanel.getStaff(), "stanExternalRepresentation");
		if (musicPanel.getStoryspaceScroll() != null) {
			musicPanel.getStoryspaceScroll().setTitle(f.getName());
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
			js.put(model.getClass().getSimpleName(), model.getJsonRepresentation());
			try {
				PrintWriter out = new PrintWriter(f);
				out.println(js.toString(2));
				out.close();
			} catch (IOException exc) { System.out.println("У нас тут жорпа с файлом " ); exc.printStackTrace(); }
		} catch (JSONException exc) { System.out.println("У нас тут жорпа с жсоном " ); exc.printStackTrace(); }
	}
}
