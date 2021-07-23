package com.example.lexis.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Word")
public class Word extends ParseObject {
    public static final String KEY_TARGET_LANGUAGE = "targetLanguage";
    public static final String KEY_TARGET_WORD = "targetWord";
    public static final String KEY_ENGLISH_WORD = "englishWord";
    public static final String KEY_TARGET_WORD_SEARCH = "targetWordSearch";
    public static final String KEY_ENGLISH_WORD_SEARCH = "englishWordSearch";
    public static final String KEY_STARRED = "isStarred";
    public static final String KEY_USER = "user";

    public String getTargetWord() {
        return getString(KEY_TARGET_WORD);
    }

    public void setTargetWord(String word) {
        put(KEY_TARGET_WORD, word);
    }

    public String getTargetWordLower() {
        return getString(KEY_TARGET_WORD_SEARCH);
    }

    public void setTargetWordLower(String word) {
        put(KEY_TARGET_WORD_SEARCH, word);
    }

    public String getEnglishWord() {
        return getString(KEY_ENGLISH_WORD);
    }

    public void setEnglishWord(String word) {
        put(KEY_ENGLISH_WORD, word);
    }

    public String getEnglishWordLower() {
        return getString(KEY_ENGLISH_WORD_SEARCH);
    }

    public void setEnglishWordLower(String word) {
        put(KEY_ENGLISH_WORD_SEARCH, word);
    }

    public String getTargetLanguage() {
        return getString(KEY_TARGET_LANGUAGE);
    }

    public void setTargetLanguage(String language) {
        put(KEY_TARGET_LANGUAGE, language);
    }

    public Boolean getIsStarred() {
        return getBoolean(KEY_STARRED);
    }

    public void setIsStarred(Boolean starred) {
        put(KEY_STARRED, starred);
    }

    public void toggleIsStarred() {
        put(KEY_STARRED, !getIsStarred());
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }
}
