package com.example.lexis.fragments;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.StrictMode;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.lexis.R;
import com.example.lexis.databinding.FragmentArticleBinding;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.parse.ParseUser;

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
    List<Integer> translatedIndices = new ArrayList<>();
    List<String> originalWords = new ArrayList<>();

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
        fetchWikipediaArticle("Facebook");
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

        client.get(WIKI_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONObject query = jsonObject.getJSONObject("query");
                    JSONObject pages = query.getJSONObject("pages");
                    JSONObject article = pages.getJSONObject(pages.keys().next());

                    // get intro paragraph of article, do some preprocessing
                    String intro = article.getString("extract");
                    intro = intro.replace("\n", "\n\n");

                    // TODO: deal with punctuation around words (comma, parenthesis, period)
                    String[] words = intro.split("\\s+"); // split text on whitespace
                    translateWordsOnInterval(words, 3, 60);
                    SpannableStringBuilder translatedContent = styleTranslatedWords(words);

                    binding.tvTitle.setText(article.getString("title"));
                    binding.tvBody.setText(translatedContent);
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

    private void translateWordsOnInterval(String[] words, int start, int interval) {
        String targetLanguage = ParseUser.getCurrentUser().getString("targetLanguage");
        for (int i = start; i < words.length; i += interval) {
            translatedIndices.add(i);
            originalWords.add(words[i]);
            words[i] = translateSingleWord(words[i], targetLanguage);
        }
    }

    private SpannableStringBuilder styleTranslatedWords(String[] words) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        int curr = 0;
        for (int i = 0; i < words.length; i++) {
            int start = spannableStringBuilder.length();
            spannableStringBuilder.append(words[i] + " ");

            if (curr < translatedIndices.size() && translatedIndices.get(curr) == i) {
                int finalCurr = curr;
                int finalI = i;

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        Rect wordPosition = getClickedWordPosition((TextView) textView, this);
                        String targetLanguage = words[finalI];
                        String english = originalWords.get(finalCurr);

                        launchWordDialog(targetLanguage, english, wordPosition.left, wordPosition.top, wordPosition.width());
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(getResources().getColor(R.color.black));
                        ds.setUnderlineText(false);
                    }
                };
                BackgroundColorSpan highlightedSpan = new BackgroundColorSpan(getResources().getColor(R.color.mellow_apricot));

                // make text clickable & highlighted
                spannableStringBuilder.setSpan(clickableSpan, start, spannableStringBuilder.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStringBuilder.setSpan(highlightedSpan, start, spannableStringBuilder.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                curr += 1;
            }
        }

        return spannableStringBuilder;
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

    public String translateSingleWord(String originalWord, String targetLanguage) {
        Translation translation = translate.translate(originalWord, Translate.TranslateOption.targetLanguage(targetLanguage), Translate.TranslateOption.model("base"));
        return translation.getTranslatedText();
    }

    private void launchWordDialog(String targetLanguage, String english, int left, int top, int width) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        WordDialogFragment wordDialogFragment = WordDialogFragment.newInstance(targetLanguage, english, left, top, width);
        wordDialogFragment.show(fm, "fragment_dialog");
    }

    // https://stackoverflow.com/questions/11905486/how-get-coordinate-of-a-clickablespan-inside-a-textview
    private Rect getClickedWordPosition(TextView parentTextView, ClickableSpan clickedText) {
        // Initialize global value
        Rect parentTextViewRect = new Rect();

        // Initialize values for the computing of clickedText position
        SpannableString completeText = (SpannableString)(parentTextView).getText();
        Layout textViewLayout = parentTextView.getLayout();

        double startOffsetOfClickedText = completeText.getSpanStart(clickedText);
        double endOffsetOfClickedText = completeText.getSpanEnd(clickedText);
        double startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int)startOffsetOfClickedText);
        double endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int)endOffsetOfClickedText);


        // Get the rectangle of the clicked text
        int currentLineStartOffset = textViewLayout.getLineForOffset((int)startOffsetOfClickedText);
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);


        // Update the rectangle position to his real position on screen
        int[] parentTextViewLocation = {0,0};
        parentTextView.getLocationOnScreen(parentTextViewLocation);

        double parentTextViewTopAndBottomOffset = (
                parentTextViewLocation[1] -
                        parentTextView.getScrollY() +
                        parentTextView.getCompoundPaddingTop()
        );

        parentTextViewRect.top += parentTextViewTopAndBottomOffset;
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;


        parentTextViewRect.left += (
                parentTextViewLocation[0] +
                        startXCoordinatesOfClickedText +
                        parentTextView.getCompoundPaddingLeft() -
                        parentTextView.getScrollX()
        );
        parentTextViewRect.right = (int) (
                parentTextViewRect.left +
                        endXCoordinatesOfClickedText -
                        startXCoordinatesOfClickedText
        );

        return parentTextViewRect;
    }
}