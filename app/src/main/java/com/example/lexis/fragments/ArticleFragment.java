package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.lexis.R;
import com.example.lexis.databinding.FragmentArticleBinding;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Headers;

public class ArticleFragment extends Fragment {

    private static final String TAG = "ArticleFragment";
    private static final String TOP_STORIES_URL = "https://api.nytimes.com/svc/topstories/v2/%s.json";
    private static final String WIKI_URL = "https://en.wikipedia.org/w/api.php";

    FragmentArticleBinding binding;
    Translate translate;

    public ArticleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentArticleBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("action", "query");
        params.put("format", "json");
        params.put("titles", "Facebook");
        params.put("prop", "extracts");
        params.put("explaintext", true);
        params.put("exintro", true);

        client.get(WIKI_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONObject query = jsonObject.getJSONObject("query");
                    JSONObject pages = query.getJSONObject("pages");
                    JSONObject article = pages.getJSONObject(pages.keys().next());
                    Log.i(TAG, article.toString());
                    binding.tvTitle.setText(article.getString("title"));
                    String intro = article.getString("extract");
                    intro = intro.replace("\n", "\n\n");
                    binding.tvBody.setText(intro);
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure to fetch Wikipedia article");
            }
        });

        getTranslateService();
        translate("online", "fr");
    }

    // https://medium.com/@yeksancansu/how-to-use-google-translate-api-in-android-studio-projects-7f09cae320c7
    public void getTranslateService() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getResources().openRawResource(R.raw.credentials)) {
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String translate(String originalWord, String targetLanguage) {
        Translation translation = translate.translate(originalWord, Translate.TranslateOption.targetLanguage(targetLanguage), Translate.TranslateOption.model("base"));
        return translation.getTranslatedText();
    }
}