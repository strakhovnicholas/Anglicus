package ru.rsreu.straxov.englishdictionary.controllers;

import javafx.scene.image.Image;
import ru.rsreu.straxov.englishdictionary.Word;

import java.io.File;
import java.util.stream.Collectors;


public class WordDemonstrationController extends BaseWordController {

    public void initializeComponents(Word word) {
        this.tbWord.setText(word.getWord());
        this.tbTranscription.setText(word.getTranscription());
        this.taMeaning.setText(word.getDefinitions().stream().collect(Collectors.joining("\n\n")));
        loadImagesForWord(word.getWord());
    }


    public void loadImagesForWord(String word) {
        File wordDirectory = new File("src/main/resources/images/" + word);
        if (wordDirectory.exists() && wordDirectory.isDirectory()) {
            File[] imageFiles = wordDirectory.listFiles((dir, name) -> name.endsWith(".png"));
            if (imageFiles != null) {
                for (File imageFile : imageFiles) {
                    Image image = new Image(imageFile.toURI().toString());
                    createAnchorPane(image);
                    System.out.println("С ДНЕМ РОЖДЕНИЯ ЖИДРУНАСА");
                }
            }
        }
    }
}
