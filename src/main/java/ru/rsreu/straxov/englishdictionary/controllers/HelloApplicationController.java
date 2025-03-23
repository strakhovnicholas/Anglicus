package ru.rsreu.straxov.englishdictionary.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplicationController extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplicationController.class.getResource("dictionaryPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1209, 900);
        stage.setTitle("Anglicus");
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("flag.png")));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}