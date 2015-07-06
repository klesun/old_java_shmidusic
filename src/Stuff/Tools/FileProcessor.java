package Stuff.Tools;

import Model.Explain;
import Model.Helper;
import Main.Main;
import BlockSpacePkg.StaffPkg.Staff;
import BlockSpacePkg.BlockSpace;
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
	
	public static Explain savePNG (Staff staff) {

		Explain<File> explain = makeSaveFileDialog("png", "PNG images");
		if (explain.isSuccess()) {
			File f = explain.getData();

			BufferedImage img = new BufferedImage(staff.getWidth(), staff.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.createGraphics();
			g.setColor(Color.GREEN);
			g.fillRect(15, 15, 80, 80);
			staff.drawOn(g, true);

			try {
				ImageIO.write(img, "png", f);
				return new Explain(true);
			} catch (IOException e) {
				return new Explain("Image writing exception: " + e.getMessage());
			}
		} else { return explain; }
	}


	private static Explain<File> makeSaveFileDialog(String ext, String description) {
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { return f.getAbsolutePath().endsWith("." + ext) || f.isDirectory(); }
			public String getDescription() { return description; }
		});

		int rVal = fileChooser.showSaveDialog(Main.window);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			File fn = fileChooser.getSelectedFile();
			if (!fileChooser.getFileFilter().accept(fn)) { fn = new File(fn + "." + ext); }
			return new Explain<>(fn);
		} else { return new Explain<>("you changed your mind, why?"); }
	}

	public static Explain<File> saveStoryspace(BlockSpace blockSpace) {
		Explain<File> explain = makeSaveFileDialog("bs.json", "BlockSpace Project Json Data");

		if (explain.isSuccess()) {
			return saveStoryspace(explain.getData(), blockSpace);
		} else { return explain; }
	}

	public static Explain<File> saveStoryspace(File f, BlockSpace blockSpace) {
		Main.window.setTitle(f.getAbsolutePath());
		return saveModel(f, blockSpace);
	}

	public static Explain saveMusicPanel(Staff staff) {
		Explain<File> explain = makeSaveFileDialog("midi.json", "Json Midi-music data");

		if (explain.isSuccess()) {
			File f = explain.getData();
			staff.getParentSheet().getScroll().setTitle(f.getName());

			return saveModel(f, staff); // TODO: use messages when fail
		} else { return explain; }
	}

	public static Explain openStoryspace(File f, BlockSpace blockSpace) {
		Main.window.setTitle(f.getAbsolutePath());
		return openModel(f, blockSpace);
	}

	public static Explain openStaff(Staff staff) {
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { return f.getAbsolutePath().endsWith(".midi.json") || f.isDirectory(); }
			public String getDescription() { return "Json Midi-music data"; }
		});
		if (fileChooser.showOpenDialog(Main.window) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			staff.getParentSheet().getScroll().setTitle(f.getName());

			return openModel(f, staff);
		} else {
			return new Explain("you changed your mind, why?");
		}
	}

	private static Explain openModel(File f, IModel model) {
		try {
			String js = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
			try {
				JSONObject jsObject = new JSONObject(js);
				if (js.contains(model.getClass().getSimpleName())) {
					model.reconstructFromJson(jsObject.getJSONObject(model.getClass().getSimpleName()));
				}

				return new Explain(true);
			} catch (JSONException exc) {
				String msg = "Failed to parse json - [" + exc.getMessage() + "]";
				Logger.error(msg);
				return new Explain(msg);
			}
		} catch (IOException exc) {
			String msg = "Failed to read file [" + exc.getClass().getSimpleName() + "] - {" + exc.getMessage() + "}";
			Logger.error(msg);
			return new Explain(msg);
		}
	}

	private static Explain saveModel(File f, IModel model) {
		JSONObject js = new JSONObject("{}").put(model.getClass().getSimpleName(), model.getJsonRepresentation()); // it hope it didnt broke
		try {
			PrintWriter out = new PrintWriter(f);
			out.println(js.toString(2));
			out.close();
			return new Explain(true);
		} catch (IOException exc) {
			String msg = "Failed to write to file [" + exc.getClass().getSimpleName() + "] - {" + exc.getMessage() + "}";
			Logger.error(msg);
			return new Explain(msg);
		}
	}
}
