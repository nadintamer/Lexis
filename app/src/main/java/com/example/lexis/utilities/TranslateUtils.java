package com.example.lexis.utilities;

import android.content.Context;
import android.os.StrictMode;

import com.example.lexis.R;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateException;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.io.InputStream;

public class TranslateUtils {
    private static Translate translate;
    private static LanguageServiceClient language;

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
    Get the NLP service using credentials JSON file for the API key.
    */
    public static void getNLPService(Context context) {
        try (InputStream is = context.getResources().openRawResource(R.raw.credentials)) {
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);
            LanguageServiceSettings languageServiceSettings =
                    LanguageServiceSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(myCredentials))
                            .build();
            language = LanguageServiceClient.create(languageServiceSettings);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /*
    Return a list of entities (e.g. names, organizations, etc.) found within the supplied text.
    */
    public static AnalyzeEntitiesResponse analyzeEntities(String text) {
        Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
        AnalyzeEntitiesRequest request =
                AnalyzeEntitiesRequest.newBuilder()
                        .setDocument(doc)
                        .setEncodingType(EncodingType.UTF16)
                        .build();

        return language.analyzeEntities(request);
    }
}
