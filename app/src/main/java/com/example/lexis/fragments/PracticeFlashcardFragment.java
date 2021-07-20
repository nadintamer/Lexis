package com.example.lexis.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.example.lexis.R;
import com.example.lexis.adapters.FlashcardsAdapter;
import com.example.lexis.databinding.FragmentPracticeFlashcardBinding;
import com.example.lexis.models.Word;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.List;

public class PracticeFlashcardFragment extends Fragment implements CardStackListener {

    private static final String TAG = "FlashcardFragment";
    private static final String ARG_LANGUAGE = "targetLanguage";
    private static final String ARG_ENGLISH = "answerInEnglish";

    FragmentPracticeFlashcardBinding binding;
    List<Word> words;
    FlashcardsAdapter adapter;
    CardStackLayoutManager cardLayoutManager;
    String targetLanguage;
    boolean answerInEnglish;

    public PracticeFlashcardFragment() {}

    public static PracticeFlashcardFragment newInstance(String targetLanguage, boolean answerInEnglish) {
        PracticeFlashcardFragment fragment = new PracticeFlashcardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LANGUAGE, targetLanguage);
        args.putBoolean(ARG_ENGLISH, answerInEnglish);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetLanguage = getArguments().getString(ARG_LANGUAGE);
            answerInEnglish = getArguments().getBoolean(ARG_ENGLISH);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPracticeFlashcardBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // fetch words in vocabulary
        if (words == null) {
            words = new ArrayList<>();
            fetchVocabulary(targetLanguage);
        }

        // set up card stack view
        adapter = new FlashcardsAdapter(this, words, answerInEnglish);
        cardLayoutManager = new CardStackLayoutManager(getActivity(), this);
        cardLayoutManager.setStackFrom(StackFrom.TopAndRight);
        cardLayoutManager.setTranslationInterval(12.0f);

        binding.stackFlashcards.setLayoutManager(cardLayoutManager);
        binding.stackFlashcards.setAdapter(adapter);

        // set up automatic swiping for buttons
        binding.btnForgot.setRippleColor(getResources().getColor(R.color.deep_champagne));
        binding.btnKnow.setRippleColor(getResources().getColor(R.color.light_cyan));

        binding.btnForgot.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .build();
            cardLayoutManager.setSwipeAnimationSetting(setting);
            binding.stackFlashcards.swipe();
        });

        binding.btnKnow.setOnClickListener(v -> {
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .setInterpolator(new AccelerateInterpolator())
                    .build();
            cardLayoutManager.setSwipeAnimationSetting(setting);
            binding.stackFlashcards.swipe();
        });

        // set up toolbar with custom back button
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar.getRoot());
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("");
            }

            binding.toolbar.getRoot().setNavigationIcon(R.drawable.back_arrow);
            Drawable navigationIcon = binding.toolbar.getRoot().getNavigationIcon();
            if (navigationIcon != null) {
                navigationIcon.setTint(getResources().getColor(R.color.black));
            }
            binding.toolbar.getRoot().setNavigationOnClickListener(v -> returnToPracticeTab());
        }

        binding.btnFinish.setOnClickListener(v -> returnToPracticeTab());
    }

    private void fetchVocabulary(String targetLanguage) {
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Word.KEY_TARGET_LANGUAGE, targetLanguage);
        query.addDescendingOrder("createdAt");
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary", e);
                return;
            }
            adapter.addAll(words);
        });
    }

    private void returnToPracticeTab() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new PracticeFragment())
                    .commit();
        }
    }

    private void finishSession() {
        binding.btnKnow.setVisibility(View.GONE);
        binding.btnForgot.setVisibility(View.GONE);
        binding.tvKnowHint.setVisibility(View.GONE);
        binding.tvForgotHint.setVisibility(View.GONE);
        binding.stackFlashcards.setVisibility(View.GONE);

        binding.tvFinished.setVisibility(View.VISIBLE);
        binding.btnFinish.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCardSwiped(Direction direction) {
        binding.stackFlashcards.setTranslationZ(0);
        binding.btnForgot.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        binding.btnKnow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        if (direction == Direction.Left) {
            adapter.add(words.get(cardLayoutManager.getTopPosition() - 1));
        }
        adapter.remove(cardLayoutManager.getTopPosition() - 1);

        if (adapter.getItemCount() == 0) {
            finishSession();
        }
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {
        binding.stackFlashcards.setTranslationZ(100);

        if (direction == Direction.Left) {
            binding.btnKnow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            binding.btnForgot.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.deep_champagne)));
        } else if (direction == Direction.Right) {
            binding.btnForgot.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            binding.btnKnow.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.light_cyan)));
        }
    }


    @Override
    public void onCardRewound() {}

    @Override
    public void onCardCanceled() {
        binding.stackFlashcards.setTranslationZ(0);
        binding.btnForgot.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        binding.btnKnow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
    }

    @Override
    public void onCardAppeared(View view, int position) {}

    @Override
    public void onCardDisappeared(View view, int position) {}
}