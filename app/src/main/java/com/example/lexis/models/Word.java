package com.example.lexis.models;

public class Word {
    String targetLanguage;
    String english;
    Boolean isStarred;

    public Word(String targetLanguage, String english) {
        this.targetLanguage = targetLanguage;
        this.english = english;
        this.isStarred = false;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getEnglish() {
        return english;
    }

    public Boolean getStarred() {
        return isStarred;
    }
}
