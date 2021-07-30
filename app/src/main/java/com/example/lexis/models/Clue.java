package com.example.lexis.models;

public class Clue {
    String text;
    boolean found;

    public Clue(String word) {
        text = word;
        found = false;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public String getText() {
        return text;
    }

    public boolean isFound() {
        return found;
    }
}
