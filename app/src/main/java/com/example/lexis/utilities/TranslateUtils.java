package com.example.lexis.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.example.lexis.R;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

public class TranslateUtils {
    private static Translate translate;
    private static NameFinderME personFinder;
    private static NameFinderME organizationFinder;
    private static NameFinderME locationFinder;

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
    Translate a single word given by originalWord into the target language.
    */
    public static String translateSingleWord(String originalWord, String targetLanguage) throws TranslateException {
        Translation translation = translate.translate(originalWord, Translate.TranslateOption.targetLanguage(targetLanguage), Translate.TranslateOption.model("base"));
        return translation.getTranslatedText(); // TODO: potentially look into providing more context?
    }

    /*
    TODO: add comment here
    */
    public static void getPersonModel(Context context) throws IOException {
        InputStream inputStreamNameFinder = context.getAssets().open("en-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);
        personFinder = new NameFinderME(model);
        saveModel(model, "personModel.txt");
    }

    /*
    TODO: add comment here
    */
    public static void getLocationModel(Context context) throws IOException {
        InputStream inputStreamNameFinder = context.getAssets().open("en-ner-location.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);
        locationFinder = new NameFinderME(model);
        saveModel(model, "locationModel.txt");
    }

    /*
    TODO: add comment here
    */
    public static void getOrganizationModel(Context context) throws IOException {
        InputStream inputStreamNameFinder = context.getAssets().open("en-ner-organization.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);
        organizationFinder = new NameFinderME(model);
        saveModel(model, "organizationModel.txt");
    }

    public static void saveModel(TokenNameFinderModel model, String filename) throws IOException {
        BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(filename));
        model.serialize(modelOut);
        modelOut.close();
    }

    /*
    TODO: add comment here
    */
    private static void saveToPreferences(Context context, TokenNameFinderModel model, String key) {
        SharedPreferences prefs = context.getSharedPreferences("nlp_models", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(model);
        prefsEditor.putString(key, json);
        prefsEditor.commit();
        prefsEditor.clear();
    }

    public static void setPersonFinder(NameFinderME personFinder) {
        TranslateUtils.personFinder = personFinder;
    }

    public static void setOrganizationFinder(NameFinderME organizationFinder) {
        TranslateUtils.organizationFinder = organizationFinder;
    }

    public static void setLocationFinder(NameFinderME locationFinder) {
        TranslateUtils.locationFinder = locationFinder;
    }

    public static NameFinderME getPersonFinder() {
        return personFinder;
    }

    public static NameFinderME getOrganizationFinder() {
        return organizationFinder;
    }

    public static NameFinderME getLocationFinder() {
        return locationFinder;
    }
}
