package com.example.lexis.models;

import java.util.List;

public class Article {
    private String title;
    private String body;
    private String source;
    private List<Integer> translatedIndices;
    private List<String> originalWords;

    public Article(String title, String body, String source) {
        this.title = title;
        this.body = body;
        this.source = source;
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

    public List<Integer> getTranslatedIndices() {
        return translatedIndices;
    }

    public List<String> getOriginalWords() {
        return originalWords;
    }
}
