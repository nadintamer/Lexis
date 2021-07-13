package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.lexis.adapters.ArticlesAdapter;
import com.example.lexis.databinding.FragmentFeedBinding;
import com.example.lexis.models.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Headers;

public class FeedFragment extends Fragment {

    FragmentFeedBinding binding;
    List<Article> articles;
    ArticlesAdapter adapter;

    private static final String TAG = "FeedFragment";
    private static final String NYT_TOP_STORIES_URL = "https://api.nytimes.com/svc/topstories/v2/%s.json";
    private static final String WIKI_ARTICLE_URL = "https://en.wikipedia.org/w/api.php";
    private static final String WIKI_TOP_ARTICLES_URL = "https://wikimedia.org/api/rest_v1/metrics/pageviews/top/en.wikipedia.org/all-access/%s/%s/%s";

    public FeedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        articles = new ArrayList<>();
        adapter = new ArticlesAdapter(this, articles);

        // fetch top wikipedia articles
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1); // yesterday
        // cal.set(2021, 6, 0); top articles for all days in June
        fetchTopWikipediaArticles(cal);

        binding.rvArticles.setAdapter(adapter);
        binding.rvArticles.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void fetchTopWikipediaArticles(Calendar date) {
        String year = String.valueOf(date.get(Calendar.YEAR));
        String month = String.format("%02d", date.get(Calendar.MONTH) + 1); // add one because months are 0-indexed
        String day;
        if (date.get(Calendar.DAY_OF_MONTH) != 0) {
            day = String.format("%02d", date.get(Calendar.DAY_OF_MONTH)); // add leading 0 if needed
        } else {
            day = "all-days";
        }

        AsyncHttpClient client = new AsyncHttpClient();
        String formattedUrl = String.format(WIKI_TOP_ARTICLES_URL, year, month, day);
        client.get(formattedUrl, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONObject items = jsonObject.getJSONArray("items").getJSONObject(0);
                    JSONArray jsonArticles = items.getJSONArray("articles");
                    for (int i = 2; i < 22; i++) { // temporary, get first 20 articles (skip main page and search)
                        JSONObject jsonArticle = jsonArticles.getJSONObject(i);
                        String title = jsonArticle.getString("article");
                        // skip wikipedia special pages
                        if (title.startsWith("Wikipedia:") || title.startsWith("Special:")) continue;
                        fetchWikipediaArticle(title);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
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
                    JSONObject pages = jsonObject
                            .getJSONObject("query")
                            .getJSONObject("pages");
                    JSONObject articleObject = pages.getJSONObject(pages.keys().next());

                    String title = articleObject.getString("title");
                    String intro = articleObject.getString("extract");
                    intro = intro.replace("\n", "\n\n");

                    Article article = new Article(title, intro, "Wikipedia");
                    articles.add(article);
                    adapter.notifyItemInserted(articles.size() - 1);
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