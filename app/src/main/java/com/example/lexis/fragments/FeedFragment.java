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
import com.example.lexis.R;
import com.example.lexis.adapters.ArticlesAdapter;
import com.example.lexis.databinding.FragmentFeedBinding;
import com.example.lexis.models.Article;
import com.example.lexis.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // only fetch articles if we haven't already fetched them
        if (articles == null) {
            articles = new ArrayList<>();
            fetchTopWikipediaArticles(Utils.getYesterday(), false);
        }

        // set up recyclerView
        adapter = new ArticlesAdapter(this, articles);
        binding.rvArticles.setAdapter(adapter);
        binding.rvArticles.setLayoutManager(new LinearLayoutManager(getActivity()));

        // set up pull to refresh
        binding.swipeContainer.setOnRefreshListener(() -> {
            adapter.clear();
            fetchTopWikipediaArticles(Utils.getYesterday(), false);
            binding.swipeContainer.setRefreshing(false);
        });
        binding.swipeContainer.setColorSchemeResources(R.color.tiffany_blue,
                R.color.light_cyan,
                R.color.orange_peel,
                R.color.mellow_apricot);
    }

    /*
    Fetch the top Wikipedia articles for the provided timeframe and add each of them to the
    articles list. If allDays is true, the top articles for all days of the given month and year
    will be fetched. If it is false, only the top articles for the given day will be fetched.
    */
    private void fetchTopWikipediaArticles(Calendar date, Boolean allDays) {
        String year = String.valueOf(date.get(Calendar.YEAR));
        // add leading 0 to month and day if needed
        String month = String.format(Locale.getDefault(), "%02d", date.get(Calendar.MONTH) + 1); // months are 0-indexed
        String day = String.format(Locale.getDefault(), "%02d", date.get(Calendar.DAY_OF_MONTH));
        if (allDays) day = "all-days"; // get top articles for all days of the month

        AsyncHttpClient client = new AsyncHttpClient();
        String formattedUrl = String.format(WIKI_TOP_ARTICLES_URL, year, month, day);
        client.get(formattedUrl, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONObject items = jsonObject.getJSONArray("items").getJSONObject(0);
                    JSONArray jsonArticles = items.getJSONArray("articles");

                    // temporary, get first 20 content articles
                    // (skip first two, which are main page and search)
                    for (int i = 2; i < 22; i++) {
                        JSONObject jsonArticle = jsonArticles.getJSONObject(i);
                        String title = jsonArticle.getString("article");
                        if (title.startsWith("Wikipedia:") || title.startsWith("Special:")) {
                            continue; // skip special Wikipedia pages
                        }
                        fetchWikipediaArticle(title);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to fetch article", throwable);
            }
        });

    }

    /*
    Fetch the Wikipedia article with the given title from the API, create a new Article object
    and add it to the list of articles.
    */
    private void fetchWikipediaArticle(String title) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("action", "query");
        params.put("format", "json");
        params.put("titles", title);
        params.put("prop", "extracts");
        params.put("explaintext", true); // get plain text representation
        params.put("exintro", true); // only get intro paragraph

        client.get(WIKI_ARTICLE_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONObject pages = jsonObject
                            .getJSONObject("query")
                            .getJSONObject("pages");
                    JSONObject articleObject = pages.getJSONObject(pages.keys().next());

                    // extract information from JSON object and do text pre-processing
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