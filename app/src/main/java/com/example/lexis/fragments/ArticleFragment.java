package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class ArticleFragment extends Fragment {

    private static final String TAG = "ArticleFragment";
    private static final String TOP_STORIES_URL = "https://api.nytimes.com/svc/topstories/v2/%s.json";
    private static final String WIKI_URL = "https://en.wikipedia.org/w/api.php";

    FragmentArticleBinding binding;
    Translate translate;
    String[] words;
    List<Pair<Integer, String>> translatedIndices = new ArrayList<>();

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
        getTranslateService();

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

                    // TODO: deal with punctuation around words (comma, parenthesis, period)
                    words = intro.split("\\s+"); // split text on whitespace

                    for (int i = 3; i < words.length; i += 20) {
                        translatedIndices.add(new Pair<>(i, words[i]));
                        words[i] = translate(words[i], "fr");
                    }

                    binding.tvBody.setText(embedTranslatedWords());
                    binding.tvBody.setMovementMethod(LinkMovementMethod.getInstance());
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure to fetch Wikipedia article");
            }
        });
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

    private SpannableStringBuilder embedTranslatedWords() {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        int curr = 0;
        for (int i = 0; i < words.length; i++) {
            int start = spannableStringBuilder.length();
            spannableStringBuilder.append(words[i] + " ");
            if (curr < translatedIndices.size() && translatedIndices.get(curr).first == i) {
                int finalCurr = curr;
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        Toast.makeText(getActivity(), translatedIndices.get(finalCurr).second, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(getResources().getColor(R.color.black));
                        ds.setUnderlineText(false);
                    }
                };

                spannableStringBuilder.setSpan(clickableSpan, start, spannableStringBuilder.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.mellow_apricot)), start, spannableStringBuilder.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                curr += 1;
            }
        }

        return spannableStringBuilder;
    }
}