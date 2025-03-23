package ru.rsreu.straxov.englishdictionary.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import ru.rsreu.straxov.englishdictionary.Word;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DictionaryController {

    @FXML
    private VBox wordsContainer;

    private static final String FILE_PATH = "src/main/resources/ru/rsreu/straxov/englishdictionary/progress.ser";

    @FXML
    private Hyperlink wordHyperLink;

    @FXML
    private Label addWordLabel;

    @FXML
    private ScrollPane scrollPainWords;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField tbSearch;

    private TitledPane titledPane;

    private VBox content;

    private Label label;

    private boolean optionsButtonPressed = false;

    @FXML
    private void initialize() {
        try {
            addNewWordToDictionary();
            tbSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!this.tbSearch.getText().equals("")) {
                    this.addWordLabel.setVisible(true);
                    this.wordHyperLink.setText(newValue);
                }
                else
                {
                    this.addWordLabel.setVisible(false);
                    this.wordHyperLink.setText("");
                }
            });
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    @FXML
    private void forwardToCreateWordViaHyperLink(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("wordPageCreation.fxml"));
        Parent root = loader.load();

        WordCreationController controller = loader.getController();
        controller.initializeWord(this.tbSearch.getText());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setResizable(false);
        stage.setScene(new Scene(root, 1209, 900));
    }

    @FXML
    private void forwardToCreateWord(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("wordPageCreation.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1209, 900);
            stage.setResizable(false);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNewWordToDictionary() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(FILE_PATH);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        List<Word> words = (List<Word>) objectInputStream.readObject();
        objectInputStream.close();

        words.forEach(word -> {
            Accordion accordion = new Accordion();

            titledPane = new TitledPane();
            titledPane.setFont(new Font(23));

            Text wordText = new Text(word.getWord());
            wordText.setFill(Color.web("#0303FF"));

            Text transcriptionText = new Text(String.format(" [%s]", word.getTranscription()));
            transcriptionText.setFill(Color.RED);

            HBox titleBox = new HBox(wordText, transcriptionText);
            titledPane.setGraphic(titleBox);

            StringBuilder definitionsBuilder = new StringBuilder();
            for (String definition : word.getDefinitions()) {
                definitionsBuilder.append(definition).append("\n\n");
            }

            Text definitionsText = getText(word, definitionsBuilder);

            titledPane.setContent(definitionsText);

            accordion.getPanes().add(titledPane);
            wordsContainer.getChildren().add(accordion);


        });
        wordsContainer.setSpacing(5);
    }

    @NotNull
    private Text getText(Word word, StringBuilder definitionsBuilder) {
        Text definitionsText = new Text(definitionsBuilder.toString());
        definitionsText.setWrappingWidth(500);
        definitionsText.setStyle("-fx-font-size: 16px;");

        definitionsText.setOnMouseClicked(event -> {
            try {
                setDemonstrationComponents(word, event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return definitionsText;
    }


    private void setDemonstrationComponents(Word word, MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("wordPageDemonstration.fxml"));
        Parent root = loader.load();

        WordDemonstrationController controller = loader.getController();
        controller.initializeComponents(word);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setResizable(false);
        stage.setScene(new Scene(root, 1209, 900));
    }

    @FXML
    void rollWindow(MouseEvent event) {
        if (!optionsButtonPressed) {
            Rectangle dimBackground = new Rectangle(anchorPane.getWidth(), anchorPane.getHeight());
            dimBackground.setFill(Color.rgb(0, 0, 0, 0.5));
            dimBackground.setMouseTransparent(false);
            dimBackground.setOnMouseClicked(e -> closeOptions());
            dimBackground.setId("dimBackground");

            anchorPane.getChildren().add(dimBackground);

            VBox box = new VBox();
            box.setStyle("-fx-background-color: white; -fx-pref-width: 300; -fx-pref-height: 100; -fx-padding: 20 20 20 20");

            Label label = new Label("Options");
            label.setFont(new Font(33));
            box.getChildren().add(label);

            Button button = new Button("My progress");
            button.setLayoutX(40);
            button.setStyle("-fx-background-color: #0303FF; /* Зеленый фон */\n" +
                    "    -fx-text-fill: white; /* Белый текст */\n" +
                    "    -fx-border-radius: 40;\n" +
                    "    -fx-background-radius: 40;\n" +
                    "    -fx-font-size: 21px;\n" +
                    "    -fx-font-weight: bold;");

            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #000099; /* Светло-синий фон */\n" +
                    "    -fx-text-fill: white; /* Белый текст */\n" +
                    "    -fx-border-radius: 40;\n" +
                    "    -fx-background-radius: 40;\n" +
                    "    -fx-font-size: 21px;\n" +
                    "    -fx-font-weight: bold;"));

            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #0303FF; /* Синий фон */\n" +
                    "    -fx-text-fill: white; /* Белый текст */\n" +
                    "    -fx-border-radius: 40;\n" +
                    "    -fx-background-radius: 40;\n" +
                    "    -fx-font-size: 21px;\n" +
                    "    -fx-font-weight: bold;"));
            button.setOnAction(this::loadNewPage);
            box.getChildren().add(button);

            anchorPane.getChildren().add(box);

            box.setTranslateX(-300);

            TranslateTransition transition = new TranslateTransition(Duration.millis(1000), box);
            transition.setToX(0);

            transition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

            transition.play();

            optionsButtonPressed = true;
        } else {

            closeOptions();
        }
    }


    private void closeOptions() {
        VBox box = (VBox) anchorPane.getChildren().get(anchorPane.getChildren().size() - 1);
        Rectangle dimBackground = (Rectangle) anchorPane.lookup("#dimBackground");

        TranslateTransition transition = new TranslateTransition(Duration.millis(1000), box);
        transition.setToX(-box.getWidth());
        transition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        transition.play();

        transition.setOnFinished(e -> {
            anchorPane.getChildren().remove(box);
            anchorPane.getChildren().remove(dimBackground);
        });

        optionsButtonPressed = false;
    }
    private void loadNewPage(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("graph.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setResizable(false);
            Scene scene = new Scene(root, 1209, 900);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
