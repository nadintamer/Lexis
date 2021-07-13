package com.example.lexis.models;

import com.example.lexis.utilities.TranslateUtils;
import com.parse.ParseUser;

import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Article {
    private String title;
    private String body;
    private String source;
    private String[] words;
    private List<Integer> translatedIndices;
    private List<String> originalWords;

    public Article() {}

    public Article(String title, String body, String source) {
        this.title = title;
        this.body = body;
        this.source = source;

        translatedIndices = new ArrayList<>();
        originalWords = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getSource() {
        return source;
    }

    public String[] getWordList() {
        return words;
    }

    public List<Integer> getTranslatedIndices() {
        return translatedIndices;
    }

    public List<String> getOriginalWords() {
        return originalWords;
    }

    public void translateWordsOnInterval(int start, int interval) {
        // TODO: deal with punctuation around words (comma, parenthesis, period)
        words = body.split("\\s+");
        String targetLanguage = ParseUser.getCurrentUser().getString("targetLanguage");
        for (int i = start; i < words.length; i += interval) {
            translatedIndices.add(i);
            originalWords.add(words[i]);
            words[i] = TranslateUtils.translateSingleWord(words[i], targetLanguage);
        }
    }
}
