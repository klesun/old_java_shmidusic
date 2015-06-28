package Stuff.Tools;

import Model.ActionResult;
import Model.Combo;
import Model.Helper;
import Main.Main;
import Storyspace.Staff.Staff;
import Storyspace.Storyspace;
import Model.IModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

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

	private static JFileChooser fileChooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/");
	
	public static ActionResult savePNG (Staff staff) {

		ActionResult<File> explain = makeSaveFileDialog("png", "PNG images");
		if (explain.isSuccess()) {
			File f = explain.getData();

			BufferedImage img = new BufferedImage(staff.getWidth(), staff.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.createGraphics();
			g.setColor(Color.GREEN);
			g.fillRect(15, 15, 80, 80);
			staff.drawOn(g, true);

			try {
				ImageIO.write(img, "png", f);
				return new ActionResult(true);
			} catch (IOException e) {
				return new ActionResult("Image writing exception: " + e.getMessage());
			}
		} else { return explain; }
	}


	private static ActionResult<File> makeSaveFileDialog(String ext, String description) {
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { return f.getAbsolutePath().endsWith("." + ext) || f.isDirectory(); }
			public String getDescription() { return description; }
		});

		int rVal = fileChooser.showSaveDialog(Main.window);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			File fn = fileChooser.getSelectedFile();
			if (!fileChooser.getFileFilter().accept(fn)) { fn = new File(fn + "." + ext); }
			return new ActionResult<>(fn);
		} else { return new ActionResult<>("you changed your mind, why?"); }
	}

	public static void saveStoryspace(File f, Storyspace storyspace) {
		saveModel(f, storyspace);
		Main.window.setTitle(f.getAbsolutePath());
	}

	public static ActionResult saveMusicPanel(Staff staff) {
		ActionResult<File> explain = makeSaveFileDialog("midi.json", "Json Midi-music data");

		if (explain.isSuccess()) {
			File f = explain.getData();
			saveModel(f, staff); // TODO: use messages when fail
			staff.getParentSheet().getStoryspaceScroll().setTitle(f.getName());
			return new ActionResult(true);
		} else { return explain; }
	}

	public static void openStoryspace(File f, Storyspace storyspace) {
		openModel(f, storyspace, "");
		Main.window.setTitle(f.getAbsolutePath());
	}

	public static ActionResult openStaff(Staff staff) {
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { return f.getAbsolutePath().endsWith(".midi.json") || f.isDirectory(); }
			public String getDescription() { return "Json Midi-music data"; }
		});
		if (fileChooser.showOpenDialog(Main.window) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			openModel(f, staff, "stanExternalRepresentation");
			staff.getParentSheet().getStoryspaceScroll().setTitle(f.getName());
			return new ActionResult(true);
		} else {
			return new ActionResult("you changed your mind, why?");
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
