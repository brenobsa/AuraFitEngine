package com.iafitness.aurafitengine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlLocation = MainApplication.class.getResource("main-view.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Erro: O arquivo 'main-view.fxml' não foi encontrado.");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 1180, 700);

        stage.setTitle("AuraFit Engine - AI Personal & Periodization Dashboard");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}