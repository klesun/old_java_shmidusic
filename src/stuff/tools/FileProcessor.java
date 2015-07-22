package stuff.tools;

import jm.midi.MidiParser;
import jm.midi.SMF;
import model.Explain;
import main.Main;
import blockspace.staff.Staff;
import blockspace.BlockSpace;
import model.IModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import stuff.tools.jmusic_integration.JMusicIntegration;
import org.json.JSONException;
import org.json.JSONObject;
import stuff.tools.jmusic_integration.JmModel.JmScoreMaker;
import stuff.tools.jmusic_integration.SimpleMidiParser;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Function;

public class FileProcessor {

	// TODO: make folder of project default path
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
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith("." + ext) || f.isDirectory();
			}

			public String getDescription() {
				return description;
			}
		});

		int rVal = fileChooser.showSaveDialog(Main.window);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			File fn = fileChooser.getSelectedFile();
			if (!fileChooser.getFileFilter().accept(fn)) { fn = new File(fn + "." + ext); }
			return new Explain<>(fn);
		} else { return new Explain<>("you changed your mind, why?"); }
	}

	public static Explain<File> saveStoryspace(BlockSpace blockSpace) {
		return makeSaveFileDialog("bs.json", "BlockSpace Project Json Data")
				.ifSuccess(f -> saveModel(f, blockSpace));
	}

	public static Explain saveMusicPanel(Staff staff) {

		return makeSaveFileDialog("midi.json", "Json Midi-music data").ifSuccess(f -> {
			staff.getParentSheet().getScroll().setTitle(f.getName());
			return saveModel(f, staff); // TODO: use messages when fail
		});
	}

	public static Explain saveMidi(Staff staff) {

		return makeSaveFileDialog("mid", "MIDI binary data file").ifSuccess(f -> {

			// TODO: i believe most of code in jm.midi package is actually useless. maybe clean it one day

			SMF smf = SimpleMidiParser.staffToSmf(staff);

			try {
				OutputStream os = new FileOutputStream(f);
				smf.write(os);
			} catch (IOException exc) {
				Logger.fatal(exc, "one day i'll make it not fatal...");
			}

			return new Explain(true);
		});
	}

	public static Explain openStoryspace(File f, BlockSpace blockSpace) {
		Main.window.setTitle(f.getAbsolutePath());
		return openModel(f, blockSpace);
	}

	public static Explain openStaff(Staff staff) {
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".midi.json") || f.isDirectory();
			}
			public String getDescription() {
				return "Json Midi-music data";
			}
		});
		if (fileChooser.showOpenDialog(Main.window) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			staff.getParentSheet().getScroll().setTitle(f.getName());

			return openModel(f, staff);
		} else {
			return new Explain("you changed your mind, why?");
		}
	}

	// with rounding
	public static Explain openJMusic2(Staff staff) {
		return openJMusicAndPerform(staff, new JMusicIntegration(staff)::fillFromJm2);
	}

	// TODO: we'll need this just untill i get opening MIDI into MIDIana
	public static Explain openJMusic(Staff staff) {
		return openJMusicAndPerform(staff, new JMusicIntegration(staff)::fillFromJm);
	}

	private static Explain openJMusicAndPerform(Staff staff, Function<JSONObject, Explain> fillStaffLambda) {
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) { return f.getAbsolutePath().endsWith(".jm.json") || f.isDirectory(); }
			public String getDescription() { return "Json JMusic data"; }
		});
		if (fileChooser.showOpenDialog(Main.window) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			staff.clearStan().getParentSheet().getScroll().setTitle(f.getName());

			Explain<JSONObject> jsExplain = openJsonFile(f);
			return jsExplain.isSuccess() ? fillStaffLambda.apply(jsExplain.getData()) : jsExplain;
		} else {
			return new Explain("you changed your mind, why?");
		}
	}

	private static Explain openModel(File f, IModel model) {
		Explain<JSONObject> jsExplain = openJsonFile(f);
		if (jsExplain.isSuccess()) {

			// TODO: handle some exceptions like yu-no, data structure mismatch
			// or yu-no, js may not have key model.getClass().getSimpleName()

			if (jsExplain.getData().has(model.getClass().getSimpleName())) {
				JSONObject modelJs = jsExplain.getData().getJSONObject(model.getClass().getSimpleName());
				model.reconstructFromJson(modelJs);

				return new Explain(true);
			} else {
				return new Explain("File you provided does not have [" + model.getClass().getSimpleName() + "] key in main body, " + "" +
					"only " + Arrays.toString(JSONObject.getNames(jsExplain.getData())) + "]");
			}

		} else {
			return jsExplain;
		}
	}

	private static Explain<JSONObject> openJsonFile(File f) {
		try {
			String jsString = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
			try {
				return new Explain(new JSONObject(jsString));
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

	// i made it public only for Logger.fatal()
	public static Explain saveModel(File f, IModel model) {
		JSONObject js = new JSONObject("{}").put(model.getClass().getSimpleName(), model.getJsonRepresentation()); // it hope it didnt broke
		try {
			PrintWriter out = new PrintWriter(f);
			out.println(js.toString(2));
			out.close();
			Main.window.setTitle(f.getAbsolutePath());
			return new Explain(true);
		} catch (IOException exc) {
			String msg = "Failed to write to file [" + exc.getClass().getSimpleName() + "] - {" + exc.getMessage() + "}";
			Logger.error(msg);
			return new Explain(msg);
		}
	}
}
