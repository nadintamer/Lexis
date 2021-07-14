package com.example.lexis.fragments;

import android.graphics.Rect;
import android.os.Bundle;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import javax.annotation.Nullable;

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentArticleBinding;
import com.example.lexis.models.Article;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;

public class ArticleFragment extends Fragment {

    private static final String TAG = "ArticleFragment";
    FragmentArticleBinding binding;
    Article article;

    public ArticleFragment() {}

    public static ArticleFragment newInstance(Article article) {
        ArticleFragment frag = new ArticleFragment();
        Bundle args = new Bundle();
        args.putParcelable("article", Parcels.wrap(article));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentArticleBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        article = Parcels.unwrap(getArguments().getParcelable("article"));

        // only translate words if we haven't previously done so or if the user has changed their
        // target language since the article's translation
        String currentTargetLanguage = Utils.getCurrentTargetLanguage();
        Boolean isCorrectLanguage = article.getLanguage().equals(currentTargetLanguage);
        if (article.getWordList() == null || !isCorrectLanguage) {
            article.translateWordsOnInterval(3, 60);
        }
        SpannableStringBuilder styledContent = styleTranslatedWords(article);

        binding.tvTitle.setText(article.getTitle());
        binding.tvBody.setText(styledContent);

        // needed so that translated words are clickable
        binding.tvBody.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /*
    Return a SpannableStringBuilder consisting of the article's body text, with translated words
    highlighted and clickable to show word meaning.
    */
    private SpannableStringBuilder styleTranslatedWords(Article article) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        String[] words = article.getWordList();

        int curr = 0; // keep track of what index of the translated words we are on
        for (int i = 0; i < words.length; i++) {
            int start = spannableStringBuilder.length();
            spannableStringBuilder.append(words[i]).append(" ");

            // the index we're at represents a translated word
            if (curr < article.getTranslatedIndices().size() && article.getTranslatedIndices().get(curr) == i) {
                final int translatedWordIndex = i;
                final int originalWordIndex = curr;

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        // show word meaning dialog when clicked
                        Rect wordPosition = Utils.getClickedWordPosition((TextView) textView, this);
                        String targetLanguage = words[translatedWordIndex];
                        String english = article.getOriginalWords().get(originalWordIndex);
                        launchWordDialog(targetLanguage, english, wordPosition.left, wordPosition.top, wordPosition.width());
                        addWordToDatabase(targetLanguage, english);
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
                curr++;
            }
        }

        return spannableStringBuilder;
    }

    /*
    Add a word with the provided target language and English meanings to the Parse database to
    display in the vocabulary list later.
    */
    private void addWordToDatabase(String targetWord, String englishWord) {
        Word word = new Word();
        word.setTargetWord(targetWord);
        word.setEnglishWord(englishWord);
        word.setTargetLanguage(Utils.getCurrentTargetLanguage());
        word.setIsStarred(false);
        word.setUser(ParseUser.getCurrentUser());
        word.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "Error while saving word", e);
                return;
            }
            Log.i(TAG, "Successfully saved word: " + targetWord);
        });
    }

    /*
    Launch a word meaning dialog with the provided target language and English words, relative to
    the position of the word clicked (described by left, top, and width).
    */
    private void launchWordDialog(String targetLanguage, String english, int left, int top, int width) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        WordDialogFragment wordDialogFragment = WordDialogFragment.newInstance(targetLanguage, english, left, top, width);
        wordDialogFragment.show(fm, "fragment_dialog");
    }
}