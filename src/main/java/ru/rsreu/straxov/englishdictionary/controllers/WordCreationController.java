package ru.rsreu.straxov.englishdictionary.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;

import javafx.stage.Stage;
import javafx.util.Duration;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.rsreu.straxov.englishdictionary.Word;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;



public class WordCreationController extends BaseWordController
{
    private ScheduledExecutorService scheduler;

    private Runnable searchTask;

    @FXML
    public void initialize() {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        tbWord.textProperty().addListener((observable, oldValue, newValue) -> {
            if (searchTask != null) {
                scheduler.shutdownNow();
                scheduler = Executors.newSingleThreadScheduledExecutor();
                this.hBoxPictures.getChildren().clear();
            }
            startSearchTimer(newValue);
        });

        tbWord.setPromptText("Write the word here");
        tbWord.setFocusTraversable(false);
        tbTranscription.setPromptText("Transcription");
        tbTranscription.setFocusTraversable(false);
    }

    public void initializeWord(String word)
    {
        this.tbWord.setText(word);
    }

    private void startSearchTimer(String word) {
        System.out.println(word + " started");
        fadeInAndShow(this.wheel);
        startWheelRotation(this.wheel);
        searchTask = () -> {
            if (!word.isEmpty()) {
                try {
                    Document document = Jsoup.connect(resourcer.getString("foundWordWebsite") + word).get();
                    Element spanElement = document.selectFirst(resourcer.getString("foundWordWebsitePredicate"));

                    if (spanElement != null && !spanElement.text().contains("/")) {

                        Platform.runLater(() -> {
                            try {
                                showComponents(spanElement);
                                fadeOutAndHide(this.wheel);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    else
                        {this.similarWordsFoundButton.setVisible(true); this.tbTranscription.setText(NO_RESULTS_STRING);}
                } catch (IOException e) {
                    Platform.runLater(() -> System.err.println("Error fetching transcription: " + e.getMessage()));
                }
            }
        };

        scheduler.schedule(searchTask, 2, TimeUnit.SECONDS);
    }

    private void startWheelRotation(Node wheel) {

        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setNode(wheel);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE);
        rotateTransition.setByAngle(360);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setDuration(Duration.seconds(5));
        rotateTransition.play();
    }

    private void fadeOutAndHide(Node wheel) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(wheel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> wheel.setVisible(false));
        fadeTransition.play();
    }

    private void fadeInAndShow(Node wheel) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(wheel);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        wheel.setVisible(true);
        fadeTransition.play();
    }

    @FXML
    void showSimilarWordsPressed(MouseEvent event) throws IOException {
        if (simillarWordsTooltip == null) {
            List<String> similarWords = this.getWebSiteInformation(tbWord.getText(), resourcer.getString("similarWordsWebsite"), resourcer.getString("similarWordsWebsitePredicate"));

            ListView<String> listView = new ListView<>();
            listView.getItems().addAll(similarWords);

            listView.setOnMouseClicked(e -> {
                String selectedWord = listView.getSelectionModel().getSelectedItem();
                if (selectedWord != null) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(selectedWord);
                    clipboard.setContent(content);

                    System.out.println("Copied to clipboard: " + selectedWord);
                }
            });

            simillarWordsTooltip = new Tooltip();
            simillarWordsTooltip.setGraphic(listView);

            tooltipVisibility(true);
        }
        else
        {
            tooltipVisibility(true);
            System.out.println("Similar word pressed:");
        }

    }

    private void tooltipVisibility(boolean param) {
        Point2D p = tbWord.localToScreen(100, 100);
        simillarWordsTooltip.setAutoHide(param);
        simillarWordsTooltip.show(tbWord, p.getX(), p.getY());
    }

    @FXML
    void addWordToDictionary(MouseEvent event) throws IOException, ClassNotFoundException {
        if (!this.tbWord.getText().isEmpty()) {
            List<Word> words = loadWordsFromFile();

            Word newWord = new Word(this.tbWord.getText(),this.tbTranscription.getText(), Arrays.asList(this.taMeaning.getText().split("\n\n")));

            words.add(newWord);

            saveImagesForWord(newWord.getWord());

            saveWordsToFile(words);

            try {
                Parent root = FXMLLoader.load(getClass().getResource("dictionaryPage.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root, 1209, 900));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty input");
            alert.setHeaderText("The word is empty");
            alert.setContentText("Please enter a word to add to the dictionary");
            alert.showAndWait();
        }
    }


    private void saveWordsToFile(List<Word> words) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(FILE_PATH);
             ObjectOutputStream oos = new ObjectOutputStream(fos);) {

            oos.writeObject(words);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
