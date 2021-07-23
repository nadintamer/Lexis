package com.example.lexis.models;

import android.util.Log;

import com.example.lexis.utilities.TranslateUtils;
import com.example.lexis.utilities.Utils;
import com.google.cloud.translate.TranslateException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;

@Parcel
public class Article {

    private static final String TAG = "Article";

    private String title;
    private String body;
    private String source;
    private String language;
    private String[] words; // TODO: think about performance -- storing only translated words?
    private List<Integer> translatedIndices;
    private List<String> originalWords;

    public Article() {}

    public Article(String title, String body, String source) {
        this.title = title;
        this.body = body;
        this.source = source;

        language = "";
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

    public String getLanguage() {
        return language;
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

    /*
    Translate every interval word starting at index start into the current user's target language.
    For example, translateWordsOnInterval(0, 5) will translate every 5th word starting at index 0.
    */
    public void translateWordsOnInterval(int start, int interval) {
        // TODO: something to consider -- populate words as user scrolls, rather than all at once
        words = body.split("\\s+"); // split on whitespace
        String targetLanguage = Utils.getCurrentTargetLanguage();
        language = targetLanguage;

        NameFinderME personFinder = TranslateUtils.getPersonFinder();
        NameFinderME locationFinder = TranslateUtils.getLocationFinder();
        NameFinderME organizationFinder = TranslateUtils.getOrganizationFinder();

        for (int i = start; i < words.length; i += interval) {
            int currentIndex = i;
            String currentWord = words[currentIndex];
            String[] token = { currentWord };

            Span[] personSpans = personFinder != null ? personFinder.find(token) : new Span[]{};
            Span[] locationSpans = locationFinder != null ? locationFinder.find(token) : new Span[]{};
            Span[] organizationSpans = organizationFinder != null ? organizationFinder.find(token) : new Span[]{};

            Span[] namedEntities = ArrayUtils.addAll(personSpans, locationSpans);
            namedEntities = ArrayUtils.addAll(namedEntities, organizationSpans);

            Log.i(TAG, Arrays.toString(namedEntities));
            // move to next word until we find an alphabetical word that is not a named entity
            while (!StringUtils.isAlpha(currentWord) || namedEntities.length > 0) {
                currentIndex++;
                currentWord = words[currentIndex];
                token = new String[]{ currentWord };
                personSpans = personFinder != null ? personFinder.find(token) : new Span[]{};
                locationSpans = personFinder != null ? locationFinder.find(token) : new Span[]{};
                organizationSpans = personFinder != null ? organizationFinder.find(token) : new Span[]{};

                namedEntities = ArrayUtils.addAll(personSpans, locationSpans);
                namedEntities = ArrayUtils.addAll(namedEntities, organizationSpans);
                Log.i(TAG, Arrays.toString(namedEntities));
            }
            String stripped = Utils.stripPunctuation(currentWord, null);
            try {
                String translated = StringEscapeUtils.unescapeHtml4(TranslateUtils.translateSingleWord(stripped, targetLanguage));
                translatedIndices.add(currentIndex);
                originalWords.add(words[currentIndex]);
                words[currentIndex] = translated;
            } catch (TranslateException e) {
                Log.e(TAG, "Error translating word: " + words[currentIndex], e);
            }
        }
    }
}
