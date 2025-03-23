package ru.rsreu.straxov.englishdictionary.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.rsreu.straxov.englishdictionary.Word;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public class GraphController {

    @FXML
    private PieChart pieChart;

    private static final int TOTAL_WORDS_IN_ENGLISH = 100;
    private int wordsInDictionary;

    @FXML
    private void initialize() {
        wordsInDictionary = getWordsCountFromDictionary();

        PieChart.Data knownWordsData = new PieChart.Data("Words in Dictionary", 0);
        PieChart.Data remainingWordsData = new PieChart.Data("Remaining Words", TOTAL_WORDS_IN_ENGLISH);

        pieChart.getData().addAll(knownWordsData, remainingWordsData);

        pieChart.setTitle("Word Distribution");
        pieChart.setLabelsVisible(true);

        animatePieChart(knownWordsData, wordsInDictionary);
    }

    private void animatePieChart(PieChart.Data knownWordsData, int knownWordsFinal) {
        Duration duration = Duration.millis(300);

        Timeline timeline = new Timeline();

        KeyValue keyValueKnown = new KeyValue(knownWordsData.pieValueProperty(), knownWordsFinal);
        KeyFrame keyFrameKnown = new KeyFrame(duration, keyValueKnown);

        timeline.getKeyFrames().addAll(keyFrameKnown);

        timeline.play();
    }


    private int getWordsCountFromDictionary() {
        try (FileInputStream fileInputStream = new FileInputStream("src/main/resources/ru/rsreu/straxov/englishdictionary/progress.ser");
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            List <Word> words = (List<Word>) objectInputStream.readObject();
            return words.size();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @FXML
    protected void getBackToDictionary(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("dictionaryPage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1209, 900));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
