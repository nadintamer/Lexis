package com.example.lexis.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

@ParseClassName("Word")
public class Word extends ParseObject {
    public static final String KEY_TARGET_LANGUAGE = "targetLanguage";
    public static final String KEY_TARGET_WORD = "targetWord";
    public static final String KEY_ENGLISH_WORD = "englishWord";
    public static final String KEY_TARGET_WORD_SEARCH = "targetWordSearch";
    public static final String KEY_ENGLISH_WORD_SEARCH = "englishWordSearch";
    public static final String KEY_STARRED = "isStarred";
    public static final String KEY_USER = "user";
    public static final String KEY_SCORE = "score";
    public static final String KEY_LAST_PRACTICED = "lastPracticed";
    public static final String KEY_WORD_LENGTH = "targetWordLength";

    public static Word copyWord(Word word) {
        Word newWord = new Word();
        newWord.setTargetWord(word.getTargetWord());
        newWord.setEnglishWord(word.getEnglishWord());
        newWord.setTargetWordLower(word.getTargetWord().toLowerCase());
        newWord.setEnglishWordLower(word.getEnglishWord().toLowerCase());
        newWord.setTargetLanguage(word.getTargetLanguage());
        newWord.setIsStarred(word.getIsStarred());
        newWord.setUser(word.getUser());
        newWord.setScore(word.getScore());
        newWord.setLastPracticed(word.getLastPracticed());
        newWord.setTargetWordLength(word.getTargetWordLength());
        return newWord;
    }

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

    public int getScore() {
        return getInt(KEY_SCORE);
    }

    public void setScore(int score) {
        put(KEY_SCORE, score);
    }

    public void incrementScore() {
        put(KEY_SCORE, getScore() + 1);
    }

    public void decrementScore() {
        put(KEY_SCORE, getScore() - 1);
    }

    public Date getLastPracticed() {
        return getDate(KEY_LAST_PRACTICED);
    }

    public void setLastPracticed(Date date) {
        put(KEY_LAST_PRACTICED, date);
    }

    public int getTargetWordLength() {
        return getInt(KEY_WORD_LENGTH);
    }

    public void setTargetWordLength(int length) {
        put(KEY_WORD_LENGTH, length);
    }

    /*
    Return the length of the longest word in the word list.
    */
    public static int getLongestWord(List<Word> words) {
        int longest = words.get(0).getTargetWord().length();
        for (int i = 1; i < words.size(); i++) {
            int currentLength = words.get(i).getTargetWord().length();
            if (currentLength > longest) {
                longest = currentLength;
            }
        }
        return longest;
    }
}
