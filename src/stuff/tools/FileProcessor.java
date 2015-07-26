package stuff.tools;

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
import stuff.Midi.SimpleMidiParser;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Function;

public class FileProcessor {

	// TODO: make folder of project default path
	private static JFileChooser fileChooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/");
	
	public static Explain savePNG (Staff staff)
	{
		return makeSaveFileDialog("png", "PNG images").ifSuccess(f ->
		{
			BufferedImage img = new BufferedImage(staff.getWidth(), staff.getHeight(), BufferedImage.TYPE_INT_ARGB);
			staff.drawOn(img.getGraphics(), true);

			return Explain.tryException(() -> ImageIO.write(img, "png", f));
		});
	}

	public static Explain<File> saveStoryspace(BlockSpace blockSpace) {
		return makeSaveFileDialog("bs.json", "BlockSpace Project Json Data")
				.ifSuccess(f -> saveModel(f, blockSpace));
	}

	public static Explain saveMusicPanel(Staff staff) {

		return makeSaveFileDialog("midi.json", "Json Midi-music data").ifSuccess(f -> {
			staff.getParentSheet().getParentBlock().setTitle(f.getName());
			return saveModel(f, staff); // TODO: use messages when fail
		});
	}

	public static Explain saveMidi(Staff staff) {

		return makeSaveFileDialog("mid", "MIDI binary data file")
			.ifSuccess(f -> writeStaffMidi(staff, f));
	}

	private static Explain writeStaffMidi(Staff staff, File f)
	{
		try {
			SMF smf = SimpleMidiParser.staffToSmf(staff);
			OutputStream os = new FileOutputStream(f);
			smf.write(os);
			return new Explain(true);
		} catch (IOException exc) {
			return new Explain("Failed to write Staff to file", exc);
		}
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
			staff.getParentSheet().getParentBlock().setTitle(f.getName());

			return openModel(f, staff);
		} else {
			return new Explain(false, "you changed your mind, why?");
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
			staff.clearStan().getParentSheet().getParentBlock().setTitle(f.getName());

			Explain<JSONObject> jsExplain = openJsonFile(f);
			return jsExplain.isSuccess() ? fillStaffLambda.apply(jsExplain.getData()) : jsExplain;
		} else {
			return new Explain(false, "you changed your mind, why?");
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
				return new Explain(false, "File you provided does not have [" + model.getClass().getSimpleName() + "] key in main body, " + "" +
					"only " + Arrays.toString(JSONObject.getNames(jsExplain.getData())) + "]");
			}

		} else {
			return jsExplain;
		}
	}

	private static Explain<JSONObject> openJsonFile(File f) {
		return readTextFromFile(f).ifSuccess(jsString -> {
			try {
				JSONObject jsonParse = new JSONObject(jsString);
				return new Explain(jsonParse);
			} catch (JSONException exc) {
				return new Explain(false, "Failed to parse json - [" + exc.getMessage() + "]");
			}
		});
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
		} else { return new Explain<>(false, "you changed your mind, why?"); }
	}

	// i made it public only for Logger.fatal()
	public static Explain saveModel(File f, IModel model)
	{
		JSONObject js = new JSONObject("{}")
			.put(model.getClass().getSimpleName(), model.getJsonRepresentation());

		return writeTextToFile(js.toString(2), f);
	}

	private static Explain writeTextToFile(String text, File f)
	{
		try {
			PrintWriter out = new PrintWriter(f);
			out.println(text);
			out.close();
		} catch (IOException exc) {
			return new Explain("Failed to write text to file ", exc);
		}

		Main.window.setTitle(f.getAbsolutePath());
		return new Explain(true);
	}

	private static Explain<String> readTextFromFile(File f)
	{
		try {
			String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
			return new Explain<>(text);
		} catch (IOException exc) {
			return new Explain<>("Failed to read text from file ", exc);
		}
	}
}
