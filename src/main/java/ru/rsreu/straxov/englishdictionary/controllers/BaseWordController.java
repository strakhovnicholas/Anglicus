package ru.rsreu.straxov.englishdictionary.controllers;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import prutzkow.resourcer.ProjectResourcer;
import prutzkow.resourcer.Resourcer;
import ru.rsreu.straxov.englishdictionary.AdvancedSoundPlayer;
import ru.rsreu.straxov.englishdictionary.Word;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BaseWordController {

    protected FileChooser fileChooser;
    protected static Resourcer resourcer = ProjectResourcer.getInstance();

    protected static final String FILE_PATH = "src/main/resources/ru/rsreu/straxov/englishdictionary/progress.ser";

    @FXML
    protected TextArea taMeaning;

    @FXML
    protected Button getBackButton;

    @FXML
    protected HBox hBoxPictures;

    @FXML
    private ImageView imgVwBack;

    @FXML
    protected ImageView wheel;

    @FXML
    protected TextField tbTranscription;

    @FXML
    protected TextField tbWord;

    @FXML
    protected Button addOwnPictureButton;

    @FXML
    protected Button similarWordsFoundButton;

    @FXML
    protected AnchorPane mainAnchorPane;


    protected static final String NO_RESULTS_STRING = "No transcription found";

    protected Tooltip simillarWordsTooltip;

    protected void showComponents(Element spanElement) throws IOException {
        this.tbTranscription.setText(spanElement.text());
        this.similarWordsFoundButton.setVisible(false);
        setTextAreaContent();
        setPicture(this.tbWord.getText());
    }

    private void setTextAreaContent() throws IOException {
        StringBuilder sb = new StringBuilder();

        Map<String, HashMap<String, List<String>>> sortedMap = new TreeMap<>(getDefinition(this.tbWord.getText()));
        sortedMap.forEach((key, value) -> {
            value.forEach((definition, examples) -> {
                String exampleText = IntStream.range(0, examples.size())
                        .mapToObj(i -> "    •" + examples.get(i))
                        .collect(Collectors.joining("\n"));
                sb.append(String.format("%s\n%s\n%s\n",key, definition, exampleText));
                sb.append("\n");
            });
        });
        this.taMeaning.setText(sb.toString().replace("\n\n\n","\n\n"));

    }

    protected static Map<String, HashMap<String,List<String>>> getDefinition(String word) {
        HashMap<String, HashMap<String,List<String>>> definitionsMap = new HashMap<>();
        try {
            Document doc = Jsoup.connect(resourcer.getString("definitionsWebSite") + word).get();
            Elements definitionBlocks = doc.select(resourcer.getString("definitionBlock"));
            for (Element block : definitionBlocks) {
                Element levelElement = block.selectFirst(resourcer.getString("definitionLevel"));
                if (levelElement != null) {
                    String level = levelElement.text();
                    if (level.matches("A1|A2|B1|B2|C1|C2")) {
                        getWordInfo(block, definitionsMap, level);
                    }
                }
                else
                {
                    String level = "Other cases";
                    getWordInfo(block, definitionsMap, level);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definitionsMap;
    }

    protected static void getWordInfo(Element block, HashMap<String, HashMap<String, List<String>>> definitionsMap, String level) {
        if (!definitionsMap.containsKey(level)) {
            definitionsMap.put(level, new HashMap<>());
            getDefinitionAndWriteToMap(block, definitionsMap, level);
        }
        else
        {
            getDefinitionAndWriteToMap(block, definitionsMap, level);
        }
    }

    protected static void getDefinitionAndWriteToMap(Element block, HashMap<String, HashMap<String,List<String>>> definitionsMap, String level) {
        Element definition = block.selectFirst(resourcer.getString("definitionText"));
        Elements blockExamples = block.select(resourcer.getString("exampleBlock"));
        if (definition != null && !definitionsMap.get(level).containsKey(definition.text())) {
            definitionsMap.get(level).put(Character.toUpperCase(definition.text().charAt(0)) + definition.text().substring(1),blockExamples.stream().map(e -> e.text()).collect(Collectors.toList()));
        }
    }

    private void setPicture(String word) throws IOException {
        String url = "https://emojigraph.org/search/?q="+ word + "&searchLang=en";
        Element correctEmoji = null;
        System.out.println(url);
        Document doc = Jsoup.connect(url).get();

        Elements emojiElements = doc.select("#search__first > div > div > div.col-12.col-lg-8 > ul > li");
        for(Element emoji : emojiElements) {
            if (emoji.text().toLowerCase().contains(word.toLowerCase()))
            {
                correctEmoji = emoji;
                break;
            }
        }
        String emojiName = correctEmoji.text().replaceAll("[^\\w\\s]", "").trim();

        String emojiUrl = "https://emojigraph.org/"+ emojiName.toLowerCase().replace(" ", "-") + "/";
        Document emojiDoc = Jsoup.connect(emojiUrl).get();

        Elements imgElement = emojiDoc.select("body > section > div.container > div > div.col-12.col-lg-8 > div.row > div:nth-child(2) > div > img"); // Или укажите более точный селектор
        if (imgElement != null) {
            String imgSrc = imgElement.attr("src");

            String fullImgUrl = "https://emojigraph.org" + imgSrc;
            imageCreation(fullImgUrl);
        } else {
            System.out.println("Элемент <img> с атрибутом src не найден.");
        }
    }

    protected void imageCreation(String src) throws IOException {
        Image image = new Image(src);
        createAnchorPane(image);
    }

    protected void createAnchorPane(Image image) {

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(140);
        imageView.setFitWidth(140);

        Rectangle clip = new Rectangle(140, 140);
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        imageView.setClip(clip);

        System.out.println(imageView.getFitHeight());
        System.out.println(imageView.getFitWidth());
        imageView.setLayoutX(4);
        imageView.setLayoutY(4);


        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setMaxWidth(140);
        anchorPane.setMaxHeight(140);
        anchorPane.setPrefHeight(140);
        anchorPane.setPrefWidth(140);

        anchorPane.setStyle("-fx-border-color: #FF0000; -fx-border-width: 2; -fx-border-radius: 20; -fx-border-style: solid;");
        anchorPane.getChildren().add(imageView);
        hBoxPictures.getChildren().add(anchorPane);


        Button buttonDeletePicture = new Button();
        buttonDeletePicture.setText("✖");
        buttonDeletePicture.setLayoutX(110);
        buttonDeletePicture.setLayoutY(10);
        anchorPane.getChildren().add(buttonDeletePicture);


        buttonDeletePicture.setOnMouseClicked(event -> {
            hBoxPictures.getChildren().remove(anchorPane);
        });


        anchorPane.setOnMouseClicked(event -> {
            modalWindow(imageView);
        });
    }

    protected void modalWindow(ImageView oldImageView) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Рисование");

        modalStage.setResizable(false);

        modalStage.setOnCloseRequest(event -> {
            modalStage.close();
            mainAnchorPane.setEffect(null);
        });


        mainAnchorPane.setEffect(new GaussianBlur());

        modalStage.setOnHidden(event -> mainAnchorPane.setEffect(null));


        BorderPane borderPane = new BorderPane();


        ToolBar toolBarHorizontal = new ToolBar();
        ToolBar toolBarVertical = new ToolBar();
        toolBarVertical.setOrientation(Orientation.VERTICAL);

        Canvas canvas = new Canvas(560, 560);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        ImageView imageView = new ImageView(oldImageView.getImage());
        imageView.setFitHeight(560);
        imageView.setFitWidth(560);


        StackPane stackPane = new StackPane();
        stackPane.setMaxHeight(420);
        stackPane.setMaxWidth(420);
        stackPane.getChildren().addAll(imageView, canvas);


        AtomicBoolean isErasing = new AtomicBoolean(false);


        Button loadImageButton = new Button("Загрузить изображение");
        loadImageButton.setOnAction(event -> {
            fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите изображение");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            File file = fileChooser.showOpenDialog(modalStage);
            if (file != null) {
                try {
                    Image newImage = new Image(file.toURI().toString());
                    imageView.setImage(newImage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showErrorDialog("Ошибка загрузки изображения", "Не удалось загрузить изображение.");
                }
            }
        });

        toolBarHorizontal.getItems().add(loadImageButton);


        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        toolBarHorizontal.getItems().add(new Label("Цвет:"));
        toolBarHorizontal.getItems().add(colorPicker);


        Slider brushSizeSlider = new Slider(1, 10, 1);


        Button eraseButton = new Button("Стерка");
        eraseButton.setOnAction(event -> {
            isErasing.set(true);
            gc.setGlobalAlpha(1.0);
            gc.setLineWidth(brushSizeSlider.getValue());
        });
        toolBarVertical.getItems().add(eraseButton);


        Button drawButton = new Button("Рисовать");
        drawButton.setOnAction(event -> {
            isErasing.set(false);
            gc.setGlobalAlpha(1.0);
            gc.setLineWidth(brushSizeSlider.getValue());
        });
        toolBarVertical.getItems().add(drawButton);

        Button clearButton = new Button("Очистить");
        clearButton.setOnAction(event -> gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()));
        toolBarVertical.getItems().add(clearButton);

        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(event -> {
            WritableImage snapshotImage = new WritableImage((int) stackPane.getWidth(), (int) stackPane.getHeight());
            stackPane.snapshot(null, snapshotImage);

            oldImageView.setImage(snapshotImage);

            modalStage.close();
        });


        toolBarHorizontal.getItems().add(saveButton);

        toolBarHorizontal.getItems().add(brushSizeSlider);

        borderPane.setTop(toolBarHorizontal);
        borderPane.setLeft(toolBarVertical);

        gc.setLineWidth(brushSizeSlider.getValue());
        gc.setStroke(colorPicker.getValue());

        canvas.setOnMousePressed(event -> {
            if (isErasing.get()) {
                gc.clearRect(event.getX() - brushSizeSlider.getValue() / 2,
                        event.getY() - brushSizeSlider.getValue() / 2,
                        brushSizeSlider.getValue(), brushSizeSlider.getValue());
            } else {
                gc.beginPath();
                gc.moveTo(event.getX(), event.getY());
                gc.stroke();
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (isErasing.get()) {
                gc.clearRect(event.getX() - brushSizeSlider.getValue() / 2,
                        event.getY() - brushSizeSlider.getValue() / 2,
                        brushSizeSlider.getValue(), brushSizeSlider.getValue());
            } else {
                gc.lineTo(event.getX(), event.getY());
                gc.stroke();
            }
        });

        colorPicker.setOnAction(event -> gc.setStroke(colorPicker.getValue()));

        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> gc.setLineWidth(newVal.doubleValue()));

        borderPane.setCenter(stackPane);

        Scene modalScene = new Scene(borderPane, 700, 700);
        modalStage.setScene(modalScene);

        modalStage.showAndWait();
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

    protected static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void createOwnImage(ActionEvent event) {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.bmp"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        File file = fileChooser.showOpenDialog((Stage) mainAnchorPane.getScene().getWindow());
        if (file != null) {
            try {
                Image newImage = new Image(file.toURI().toString());
                createAnchorPane(newImage);
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorDialog("Ошибка загрузки изображения", "Не удалось загрузить изображение.");
            }
        }
    }

    protected List<String> getWebSiteInformation(String word, String webSiteURL, String predicate) throws IOException {
        Document document = Jsoup.connect(webSiteURL + word).get();
        Elements searchedElements = document.select(predicate);
        return searchedElements.stream().map(elem -> elem.text()).collect(Collectors.toList());
    }

    @FXML
    protected void pronounce(ActionEvent event) {
        Platform.runLater(() -> {
            AdvancedSoundPlayer.playSound(resourcer.getString("pronunciationWebsite") + tbWord.getText().toLowerCase() + resourcer.getString("mp3Format"));
        });
    }

    protected void saveImagesForWord(String word) {
        File wordDirectory = new File("src/main/resources/images/" + word);
        if (!wordDirectory.exists()) {
            wordDirectory.mkdirs();
        }

        int imageIndex = 1;
        for (Node node : hBoxPictures.getChildren()) {
            if (node instanceof AnchorPane) {
                AnchorPane anchorPane = (AnchorPane) node;

                for (Node child : anchorPane.getChildren()) {
                    if (child instanceof ImageView) {
                        ImageView imageView = (ImageView) child;
                        Image image = imageView.getImage();

                        File imageFile = new File(wordDirectory, word + imageIndex + ".png");
                        saveImageToFile(image, imageFile);
                        imageIndex++;
                    }
                }
            }
        }
    }

    protected void saveImageToFile(Image image, File file) {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<Word> loadWordsFromFile() throws IOException, ClassNotFoundException {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<Word>();
        }

        List<Word> wordsList = null;

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis);) {

            wordsList = (List<Word>) ois.readObject();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
        }

        return wordsList;
    }
}
