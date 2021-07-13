package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.lexis.R;
import com.example.lexis.databinding.FragmentFeedBinding;
import com.example.lexis.models.Article;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Headers;

public class FeedFragment extends Fragment {

    FragmentFeedBinding binding;
    List<Article> articles;

    private static final String TAG = "FeedFragment";
    private static final String NYT_TOP_STORIES_URL = "https://api.nytimes.com/svc/topstories/v2/%s.json";
    private static final String WIKI_ARTICLE_URL = "https://en.wikipedia.org/w/api.php";
    private static final String WIKI_TOP_ARTICLES_URL = "https://wikimedia.org/api/rest_v1/metrics/pageviews/top/en.wikipedia.org/all-access/%d/%d/%d";

    public FeedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        articles = new ArrayList<>();
        fetchWikipediaArticle("Elon Musk");
        
        binding.btnArticleFragment.setOnClickListener(v -> {
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            final Fragment articleFragment = ArticleFragment.newInstance(articles.get(0));

            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, articleFragment)
                    .addToBackStack("")
                    .commit();
        });
    }

    private void fetchWikipediaArticle(String title) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("action", "query");
        params.put("format", "json");
        params.put("titles", title);
        params.put("prop", "extracts");
        params.put("explaintext", true);
        params.put("exintro", true);

        client.get(WIKI_ARTICLE_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONObject query = jsonObject.getJSONObject("query");
                    JSONObject pages = query.getJSONObject("pages");
                    JSONObject articleObject = pages.getJSONObject(pages.keys().next());

                    String title = articleObject.getString("title");
                    String intro = articleObject.getString("extract");
                    intro = intro.replace("\n", "\n\n");

                    Article article = new Article(title, intro, "Wikipedia");
                    articles.add(article);
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
}