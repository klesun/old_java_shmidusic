package org.shmidusic.stuff.test.html5_integration;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.shmidusic.sheet_music.SheetMusic;
import org.shmidusic.stuff.tools.StupidEcma6Adapter;
import javafx.concurrent.Worker.State;


// https://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm

public class WebViewSample extends Application {
	private Scene scene;
	@Override public void start(Stage stage) {
		// create the scene
		stage.setTitle("Web View");
		scene = new Scene(new Browser(),750,500, Color.web("#666970"));
		stage.setScene(scene);
//		scene.getStylesheets().add("webviewsample/BrowserToolbar.css");
		stage.show();
	}

	public static void main(String[] args){
		launch(args);
	}
}
class Browser extends Region {

	final WebView browser = new WebView();
	final WebEngine webEngine = browser.getEngine();

	public Browser() {
		//apply the styles
		getStyleClass().add("browser");
		// load the web page
		webEngine.load(
			StupidEcma6Adapter.toEcma5(
				WebViewSample.class.getResource("webview.html")
			).toExternalForm());
		//add the web view to the scene
		getChildren().add(browser);

		// process page loading
		webEngine.getLoadWorker().stateProperty().addListener(
			(ov, oldState, newState) -> {
				if (newState == State.SUCCEEDED) {

					JSObject win = (JSObject)webEngine.executeScript("window");
					win.setMember("someSheetMusic", new SheetMusic());
					// "After that, you can call public methods and access public fields of this object from JavaScript"

					webEngine.executeScript("$('#guzno').html('zalupa');");
					webEngine.executeScript("setTimeout(function() {$('#guzno').html('zalupa2');}, 2000);");
				}
			}
		);
	}
	private Node createSpacer() {
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		return spacer;
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

