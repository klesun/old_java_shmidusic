package org.shmidusic.stuff.tools;

import org.shmidusic.stuff.midi.standard_midi_file.SMF;
import org.klesun_model.Explain;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.sheet_music.SheetMusicComponent;
import org.shmidusic.Main;
import org.klesun_model.IModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.shmidusic.stuff.midi.NoteGuesser;
import org.json.JSONException;
import org.json.JSONObject;
import org.shmidusic.stuff.midi.SimpleMidiParser;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileProcessor {

	// TODO: make folder of project default path
	private static JFileChooser fileChooser = new JFileChooser("/home/klesun/yuzefa_git/a_opuses_json/");
	
	public static Explain savePNG (SheetMusicComponent sheetMusicComponent)
	{
		return makeSaveFileDialog("png", "PNG images").ifSuccess(f ->
		{
			BufferedImage img = new BufferedImage(sheetMusicComponent.getWidth(), sheetMusicComponent.getHeight(), BufferedImage.TYPE_INT_ARGB);
			sheetMusicComponent.paint(img.getGraphics());

			return Explain.tryException(() -> ImageIO.write(img, "png", f));
		});
	}

	public static Explain saveMusicPanel(SheetMusicComponent sheetMusicComponent) {

		return makeSaveFileDialog("mid.js", "Json Midi-music data").ifSuccess(f -> {
			return saveModel(f, sheetMusicComponent.sheetMusic); // TODO: use messages when fail
		});
	}

	public static Explain saveMidi(SheetMusicComponent sheetMusicComponent) {
		return makeSaveFileDialog("mid", "MIDI binary data file")
			.ifSuccess(f -> writeSheetMusicMidi(sheetMusicComponent.sheetMusic, f));
	}

	private static Explain writeSheetMusicMidi(SheetMusic sheetMusic, File f)
	{
		try {
			SMF smf = SimpleMidiParser.sheetMusicToSmf(sheetMusic);
			OutputStream os = new FileOutputStream(f);
			smf.write(os);
			return new Explain(true);
		} catch (IOException exc) {
			return new Explain("Failed to write Staff to file", exc);
		}
	}

	// TODO: i think, live would be easier if we cleared old ShhetMusic onstead of recreating it
	// cuz i hate messing with scroll bars and reseting Midi Receiver is also not so nice
	public static Explain openMidi(SheetMusicComponent sheetComp)
	{
		return chooseMidiFile().ifSuccess(f -> {

			try {
                SMF smf = new SMF();
                smf.read(new FileInputStream(f));

				SheetMusic sheet = new NoteGuesser(smf).generateSheetMusic(sheetComp.mainPanel::replaceSheetMusic);
				sheetComp.mainPanel.replaceSheetMusic(sheet);

//				return new Explain(false, "Not Finished Implementing Yet");
				return new Explain(true);
			} catch (IOException exc) {
				return new Explain("Failed to read midi file: " + exc.getMessage());
			}
		});
	}

	public static Explain openSheetMusic(SheetMusicComponent comp)
	{
		return chooseMidiJsonFile()
			.ifSuccess(FileProcessor::openJsonFile)
			.ifSuccess(js -> fillModelFromJson(js, new SheetMusic()))
                .whenSuccess(comp.mainPanel::replaceSheetMusic);
	}

	private static Explain<File> chooseMidiJsonFile()
	{
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".mid.js") || f.isDirectory();
			}
			public String getDescription() {
				return "Json Midi-music data";
			}
		});

		if (fileChooser.showOpenDialog(Main.window) == JFileChooser.APPROVE_OPTION) {
			return new Explain(fileChooser.getSelectedFile());
		} else {
			return new Explain(false, "you changed your mind, why?");
		}
	}

	private static Explain<File> chooseMidiFile()
	{
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".mid") || f.isDirectory();
			}
			public String getDescription() {
				return "Binary Midi music data";
			}
		});

		if (fileChooser.showOpenDialog(Main.window) == JFileChooser.APPROVE_OPTION) {
			return new Explain(fileChooser.getSelectedFile());
		} else {
			return new Explain(false, "you changed your mind, why?");
		}
	}

	private static <T extends IModel> Explain<T> fillModelFromJson(JSONObject modelJs, T model)
	{
		try {
			model.reconstructFromJson(modelJs);
			return new Explain(model);
		} catch (JSONException exc) {
			return new Explain(false, "Failed to Open Model, some Json error: " + describeException(exc));
		}
	}

	private static String describeException(Exception exc)
	{
		String traceString = Fp.traceDiff(exc, new Throwable());
		return exc.getClass().getSimpleName() + " " + exc.getMessage() + "\n\n" + traceString;
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
	public static Explain saveModel(File f, IModel model) {
		return writeTextToFile(model.getJsonRepresentation().toString(2), f);
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
