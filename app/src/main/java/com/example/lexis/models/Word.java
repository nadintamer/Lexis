package com.example.lexis.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Word")
public class Word extends ParseObject {
    public static final String KEY_TARGET_LANGUAGE = "targetLanguage";
    public static final String KEY_ENGLISH = "english";
    public static final String KEY_STARRED = "isStarred";

    public String getTargetLanguage() {
        return getString(KEY_TARGET_LANGUAGE);
    }

    public void setTargetLanguage(String word) {
        put(KEY_TARGET_LANGUAGE, word);
    }

    public String getEnglish() {
        return getString(KEY_ENGLISH);
    }

    public void setEnglish(String word) {
        put(KEY_ENGLISH, word);
    }

    public Boolean getIsStarred() {
        return getBoolean(KEY_STARRED);
    }

    public void setIsStarred(Boolean starred) {
        put(KEY_STARRED, starred);
    }
}
