package com.example.lexis.utilities;

import android.content.Context;
import android.os.StrictMode;

import com.example.lexis.R;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

public class TranslateUtils {
    private static Translate translate;
    private static NameFinderME nameFinder;

    /*
    Get the translation service using credentials JSON file for the API key.
    Adapted from: https://medium.com/@yeksancansu/how-to-use-google-translate-api-in-android-studio-projects-7f09cae320c7
    */
    public static void getTranslateService(Context context) {
        // TODO: might want to change this? not really sure if this is good
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); // permit networking calls on main thread

        // use credentials from JSON file to get the translation service
        try (InputStream is = context.getResources().openRawResource(R.raw.credentials)) {
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /*
    TODO: add comment here
    */
    public static void getNERModel(Context context) throws IOException {
        InputStream inputStreamNameFinder = context.getAssets().open("en-ner-location.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);
        nameFinder = new NameFinderME(model);
    }

    /*
    TODO: add comment here
    */
    public static NameFinderME getNameFinder() {
        return nameFinder;
    }

    /*
    Translate a single word given by originalWord into the target language.
    */
    public static String translateSingleWord(String originalWord, String targetLanguage) throws TranslateException {
        Translation translation = translate.translate(originalWord, Translate.TranslateOption.targetLanguage(targetLanguage), Translate.TranslateOption.model("base"));
        return translation.getTranslatedText(); // TODO: potentially look into providing more context?
    }
}
