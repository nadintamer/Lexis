package com.example.lexis.fragments;

import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import javax.annotation.Nullable;

import com.example.lexis.R;
import com.example.lexis.databinding.FragmentArticleBinding;
import com.example.lexis.models.Article;
import com.example.lexis.utilities.Utils;

import org.parceler.Parcels;

import java.util.Arrays;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentArticleBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        article = Parcels.unwrap(getArguments().getParcelable("article"));

        // only translate words if we haven't previously done so
        if (article.getWordList() == null) {
            Log.i(TAG, "Translating words");
            article.translateWordsOnInterval(3, 60);
        }
        SpannableStringBuilder styledContent = styleTranslatedWords(article.getWordList());

        binding.tvTitle.setText(article.getTitle());
        binding.tvBody.setText(styledContent);
        binding.tvBody.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableStringBuilder styleTranslatedWords(String[] words) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        int curr = 0; // keep track of what index of the translated words we are on
        for (int i = 0; i < words.length; i++) {
            int start = spannableStringBuilder.length();
            spannableStringBuilder.append(words[i] + " ");

            if (curr < article.getTranslatedIndices().size() && article.getTranslatedIndices().get(curr) == i) {
                final int translatedWordIndex = i;
                final int originalWordIndex = curr;

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        Rect wordPosition = Utils.getClickedWordPosition((TextView) textView, this);
                        String targetLanguage = words[translatedWordIndex];
                        String english = article.getOriginalWords().get(originalWordIndex);
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
                curr++;
            }
        }

        return spannableStringBuilder;
    }

    private void launchWordDialog(String targetLanguage, String english, int left, int top, int width) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        WordDialogFragment wordDialogFragment = WordDialogFragment.newInstance(targetLanguage, english, left, top, width);
        wordDialogFragment.show(fm, "fragment_dialog");
    }
}