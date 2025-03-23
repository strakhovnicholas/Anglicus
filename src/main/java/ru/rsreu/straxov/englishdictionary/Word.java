package ru.rsreu.straxov.englishdictionary;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Word implements Serializable {
    @Getter
    private static final long serialVersionUID = 1L;
    
    @Getter
    private String word;
    @Getter
    private String transcription;
    @Getter
    private List<String> definitions;

    @Setter
    private List<String> wordImagesData;

    public Word(String word, String transcription, List<String> definitions) {
        this.word = word;
        this.transcription = transcription;
        this.definitions = definitions;
    }

}
