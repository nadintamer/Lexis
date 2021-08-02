package com.example.lexis.models;

public class Clue {
    Word word;
    boolean found;

    public Clue(Word word) {
        this.word = word;
        found = false;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public String getText() {
        return word.getEnglishWord();
    }

    public String getSolution() {
        return word.getTargetWord();
    }

    public boolean isFound() {
        return found;
    }
}
