package org.shmidusic.stuff.test.html5_integration;

// we run javascript sheet music painter through this

import javafx.concurrent.Worker;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.stuff.tools.StupidEcma6Adapter;

import java.net.URL;

public class SheetMusicBrowser extends Region
{
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public SheetMusicBrowser(SheetMusic song)
    {
        URL baseHtmlFile = SheetMusicBrowser.class.getResource("SheetMusicPainter.html");
        String finalHtmlFile = StupidEcma6Adapter.toEcma5(baseHtmlFile).toExternalForm();

        webEngine.load(finalHtmlFile);
        getChildren().add(browser);

        // process page loading
        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) ->
        {
            if (newState == Worker.State.SUCCEEDED) {

                // file:/Z:/deleteMe/org/shmidusic/stuff/test/html5_integration/firebug-lite.js
                String firebugUrl = "https://getfirebug.com/firebug-lite.js";

                JSObject win = (JSObject)webEngine.executeScript("window");
                win.setMember("SHEET_MUSIC_FROM_JAVA", song);
                win.setMember("STD_OUT", System.out);
                // "After that, you can call public methods and access public fields of this object from JavaScript"

//                webEngine.executeScript("$('#guzno').html('zalupa');");
//                webEngine.executeScript("setTimeout(function() {$('#guzno').html('zalupa2');}, 2000);");
            }
        });
    }

    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }

    @Override protected double computePrefWidth(double height) {
        return 750;
    }

    @Override protected double computePrefHeight(double width) {
        return 500;
    }
}
