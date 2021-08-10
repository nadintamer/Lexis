package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.TextUtils;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import okhttp3.Headers;

public class FeedFragment extends Fragment {

    FragmentFeedBinding binding;
    List<Article> articles;
    ArticlesAdapter adapter;

    private static final String TAG = "FeedFragment";
    private static final String TOP_HEADLINES_URL = "https://newsapi.org/v2/top-headlines";
    private static final String NYT_ARTICLE_SEARCH_URL = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private static final String SHORT_STORIES_URL = "https://shortstories-api.herokuapp.com/stories";
    private static final String WIKI_ARTICLE_URL = "https://en.wikipedia.org/w/api.php";
    private static final String WIKI_TOP_ARTICLES_URL = "https://wikimedia.org/api/rest_v1/metrics/pageviews/top/en.wikipedia.org/all-access/%s/%s/%s";
    private static final int NUM_STORIES_TO_FETCH = 10;
    private static final int NUM_APIS = 4;

    private int numCallsCompleted = 0;

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
            showProgressBar();
            binding.rvArticles.setClickable(false);
            fetchContent();
        }

        setUpRecyclerView();
        Utils.setLanguageLogo(binding.toolbar.ivLogo);
    }

    /*
    Set up the recyclerView for displaying articles.
    */
    private void setUpRecyclerView() {
        adapter = new ArticlesAdapter(this, articles);
        binding.rvArticles.setAdapter(adapter);
        binding.rvArticles.setLayoutManager(new LinearLayoutManager(getActivity()));

        // set up pull to refresh
        binding.swipeContainer.setOnRefreshListener(() -> {
            adapter.clear();
            numCallsCompleted = 0;
            binding.rvArticles.setClickable(false);
            fetchContent();
        });
        binding.swipeContainer.setColorSchemeResources(R.color.tiffany_blue,
                R.color.light_cyan,
                R.color.orange_peel,
                R.color.mellow_apricot);
    }

    /*
    Fetch content from all integrated APIs.
    */
    private void fetchContent() {
        fetchTopWikipediaArticles(Utils.getYesterday(), false);
        fetchTopHeadlines(Arrays.asList("bbc-news", "time", "wired", "the-huffington-post"));
        fetchShortStories(NUM_STORIES_TO_FETCH);
        fetchNYTArticles();
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

                    // get first 20 content articles
                    // (skip first two, which are main page and search)
                    for (int i = 2; i < 22; i++) {
                        JSONObject jsonArticle = jsonArticles.getJSONObject(i);
                        String title = jsonArticle.getString("article");
                        fetchWikipediaArticle(title, i);
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
    private void fetchWikipediaArticle(String title, int index) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("action", "query");
        params.put("format", "json");
        params.put("titles", title);
        params.put("prop", "extracts|info");
        params.put("inprop", "url");
        params.put("explaintext", true); // get plain text representation
        params.put("exintro", true); // only get intro paragraph

        client.get(WIKI_ARTICLE_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // skip special Wikipedia pages
                    if (!title.startsWith("Wikipedia:") && !title.startsWith("Special:")) {
                        JSONObject pages = jsonObject
                                .getJSONObject("query")
                                .getJSONObject("pages");
                        JSONObject articleObject = pages.getJSONObject(pages.keys().next());

                        // extract information from JSON object and do text pre-processing
                        String source = "Wikipedia";
                        String url = articleObject.getString("fullurl");
                        String title = articleObject.getString("title");
                        String intro = articleObject.getString("extract");
                        intro = intro.replace("\n", "\n\n");

                        Article article = new Article(title, intro, source, url);
                        articles.add(article);
                        adapter.notifyItemInserted(articles.size() - 1);
                    }

                    // finished adding articles
                    if (index == 21) {
                        numCallsCompleted++;
                        checkIfFetchingCompleted();
                    }
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

    /*
    Fetch the top headlines from the provided list of sources.
    */
    private void fetchTopHeadlines(List<String> sources) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("apiKey", getString(R.string.news_api_key));
        params.put("sources", TextUtils.join(",", sources));

        client.get(TOP_HEADLINES_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray articlesArray = jsonObject.getJSONArray("articles");
                    for (int i = 0; i < articlesArray.length(); i++) {
                        JSONObject articleObject = articlesArray.getJSONObject(i);

                        String title = articleObject.getString("title");
                        String content = articleObject.getString("content");
                        int truncatedIndex = content.indexOf("[+");
                        if (truncatedIndex != -1) {
                            content = content.substring(0, truncatedIndex);
                        }
                        String source = articleObject.getJSONObject("source").getString("name");
                        String url = articleObject.getString("url");

                        Article article = new Article(title, content, source, url);
                        articles.add(article);
                        adapter.notifyItemInserted(articles.size() - 1);
                    }

                    numCallsCompleted++;
                    checkIfFetchingCompleted();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to fetch top headlines", throwable);
            }
        });
    }

    /*
    Fetch 20 random short stories.
    */
    private void fetchShortStories(int n) {
        AsyncHttpClient client = new AsyncHttpClient();
        Set<Integer> storiesAdded = new HashSet<>();

        client.get(SHORT_STORIES_URL, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    Random rand = new Random();
                    for (int i = 0; i < n; i++) {
                        // generate random index for story we haven't added yet
                        int index = rand.nextInt(jsonArray.length());
                        while (storiesAdded.contains(index)) index = rand.nextInt(jsonArray.length());
                        storiesAdded.add(index);

                        JSONObject story = jsonArray.getJSONObject(index);
                        String title = story.getString("title");
                        String content = story.getString("story") + " " + story.getString("moral");
                        String source = "Short stories";

                        Article article = new Article(title, content, source, null);
                        articles.add(article);
                        adapter.notifyItemInserted(articles.size() - 1);
                    }

                    numCallsCompleted++;
                    checkIfFetchingCompleted();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to fetch short stories", throwable);
            }
        });
    }

    /*
    Fetch recent NYT articles.
    */
    private void fetchNYTArticles() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("api-key", getString(R.string.new_york_times_api_key));

        client.get(NYT_ARTICLE_SEARCH_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray articlesArray = jsonObject
                            .getJSONObject("response")
                            .getJSONArray("docs");
                    for (int i = 0; i < NUM_STORIES_TO_FETCH; i++) {
                        JSONObject articleObject = articlesArray.getJSONObject(i);

                        String title = articleObject
                                .getJSONObject("headline")
                                .getString("main");
                        String content = articleObject.getString("lead_paragraph");
                        String source = "The New York Times";
                        String url = articleObject.getString("web_url");

                        Article article = new Article(title, content, source, url);
                        articles.add(article);
                        adapter.notifyItemInserted(articles.size() - 1);
                    }

                    numCallsCompleted++;
                    checkIfFetchingCompleted();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to fetch top headlines", throwable);
            }
        });
    }

    /*
    Check if all API calls have finished and hide progress indicator if so.
    */
    private void checkIfFetchingCompleted() {
        // shuffle articles list for variety in source ordering
        adapter.shuffle();
        if (numCallsCompleted == NUM_APIS) {
            hideProgressBar();
            binding.swipeContainer.setRefreshing(false);
            // user can only click on articles after everything has loaded to avoid crashes
            binding.rvArticles.setClickable(true);
        }
    }

    /*
    Helper functions to hide and show progress bar.
    */
    public void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        binding.progressBar.setVisibility(View.INVISIBLE);
    }
}