package org.shmidusic.stuff.test.html5_integration;

// this test class will draw sheet music like we do
// with SheetMusicComponent, but using javascript canvas

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.klesun_model.Explain;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.stuff.tools.FileProcessor;

import java.io.File;

public class DrawSheetMusic extends Application
{
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Scene scene = new Scene(new SheetMusicBrowser(getSong()), 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static SheetMusic getSong()
    {
        String fileName = "opus23_angelsmerti_sloznoyeNa4alo_ubasazvesdnajaboleznj.mid.js";
        File songFile = new File("Z:/progas/shmidusic.lv/Dropbox/yuzefa_git/a_opuses_json/" + fileName);

        Explain<SheetMusic> maybeSong = FileProcessor.openJsonFile(songFile)
                .ifSuccess(js -> FileProcessor.fillModelFromJson(js, SheetMusic::new));

        return maybeSong.dieIfFailure();
    }
}
