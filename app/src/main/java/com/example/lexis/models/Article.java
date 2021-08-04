package com.example.lexis.models;

import android.util.Log;

import com.example.lexis.utilities.TranslateUtils;
import com.example.lexis.utilities.Utils;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Article {

    private static final String TAG = "Article";

    private String title;
    private String body;
    private String source;
    private String url;
    private String language;
    private int frequency;
    private String[] words; // TODO: think about performance -- storing only translated words?
    private List<Integer> translatedIndices;
    private List<String> originalWords;

    public Article() {}

    public Article(String title, String body, String source, String url) {
        this.title = title;
        this.body = body;
        this.source = source;
        this.url = url;

        language = "";
        frequency = 0;
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

    public String getUrl() { return url; }

    public String getLanguage() {
        return language;
    }

    public int getFrequency() {
        return frequency;
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
        frequency = interval;

        for (int i = start; i < words.length; i += interval) {
            int currentIndex = i;
            String currentWord = words[currentIndex];

            AnalyzeEntitiesResponse response = TranslateUtils.analyzeEntities(currentWord);
            Entity entity = null;
            EntityMention.Type type = null;
            if (response.getEntitiesCount() > 0) {
                entity = response.getEntitiesList().get(0);
                if (entity.getMentionsCount() > 0) {
                    type = entity.getMentionsList().get(0).getType();
                }
            }

            /*
            Move to the next word until we find an alphabetical word that is not a named entity
            (specifically checking for PROPER types like person names or locations, and TYPE_UNKNOWN
            which generally refers to numbers)
            */
            while (!StringUtils.isAlpha(currentWord) || (entity != null && (
                    type == EntityMention.Type.PROPER || type == EntityMention.Type.TYPE_UNKNOWN))) {
                // we reached the last word and couldn't find anything appropriate to translate
                if (currentIndex == words.length - 1) {
                    break;
                }

                currentIndex++;
                currentWord = words[currentIndex];

                response = TranslateUtils.analyzeEntities(currentWord);
                if (response.getEntitiesCount() > 0) {
                    entity = response.getEntitiesList().get(0);
                    if (entity.getMentionsCount() > 0) {
                        type = entity.getMentionsList().get(0).getType();
                    }
                }
            }

            // we reached the last word and couldn't find anything appropriate to translate, so
            // don't translate the last word and finish translating entirely
            if (currentIndex == words.length - 1) {
                break;
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
