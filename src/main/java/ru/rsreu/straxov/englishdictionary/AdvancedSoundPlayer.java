package ru.rsreu.straxov.englishdictionary;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AdvancedSoundPlayer {
    public static void playSound(String soundUrl) {
        try {
            Media sound = new Media(soundUrl);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
            System.out.println("Воспроизведение аудио..." + soundUrl);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка воспроизведения аудио");
        }
    }
}
