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
import android.widget.Toast;

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
import java.util.Date;
import java.util.List;

import eu.davidea.flipview.FlipView;

public class PracticeFlashcardFragment extends Fragment implements CardStackListener {

    private static final String TAG = "FlashcardFragment";
    private static final String ARG_LANGUAGE = "targetLanguage";
    private static final String ARG_ENGLISH = "answerInEnglish";
    private static final String ARG_STARRED_WORDS = "starredWordsOnly";
    private static final String ARG_NUM_FLASHCARDS = "numFlashcards";

    FragmentPracticeFlashcardBinding binding;
    List<Word> words;
    FlashcardsAdapter adapter;
    CardStackLayoutManager cardLayoutManager;
    String targetLanguage;
    boolean answerInEnglish;
    boolean starredWordsOnly;
    boolean wasFlipped;
    int numFlashcards;

    public PracticeFlashcardFragment() {}

    public static PracticeFlashcardFragment newInstance(
            String targetLanguage, boolean answerInEnglish, boolean starredWordsOnly, int numFlashcards) {
        PracticeFlashcardFragment fragment = new PracticeFlashcardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LANGUAGE, targetLanguage);
        args.putBoolean(ARG_ENGLISH, answerInEnglish);
        args.putBoolean(ARG_STARRED_WORDS, starredWordsOnly);
        args.putInt(ARG_NUM_FLASHCARDS, numFlashcards);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetLanguage = getArguments().getString(ARG_LANGUAGE);
            answerInEnglish = getArguments().getBoolean(ARG_ENGLISH);
            starredWordsOnly = getArguments().getBoolean(ARG_STARRED_WORDS);
            numFlashcards = getArguments().getInt(ARG_NUM_FLASHCARDS);
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

        if (words == null) {
            words = new ArrayList<>();
            fetchVocabulary(targetLanguage, starredWordsOnly);
        }

        setUpCardStackView();
        setUpButtons();
        setUpToolbar();
    }

    /*
    Fetch the vocabulary for the provided language and add to flashcards adapter.
    */
    private void fetchVocabulary(String targetLanguage, boolean starredWordsOnly) {
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Word.KEY_TARGET_LANGUAGE, targetLanguage);
        if (starredWordsOnly) {
            query.whereEqualTo(Word.KEY_STARRED, true);
        }

        // get top cards with lowest score and least recent practice
        query.orderByAscending(Word.KEY_SCORE);
        query.addAscendingOrder(Word.KEY_LAST_PRACTICED);
        query.setLimit(numFlashcards);

        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary", e);
                return;
            }
            adapter.addAll(words);
        });
    }

    /*
    Set up the card stack view to display the flashcards.
    */
    private void setUpCardStackView() {
        adapter = new FlashcardsAdapter(this, words, answerInEnglish);
        cardLayoutManager = new CardStackLayoutManager(getActivity(), this);
        cardLayoutManager.setStackFrom(StackFrom.TopAndRight);
        cardLayoutManager.setTranslationInterval(12.0f);

        binding.stackFlashcards.setLayoutManager(cardLayoutManager);
        binding.stackFlashcards.setAdapter(adapter);
    }

    /*
    Set up buttons for automatic swiping.
    */
    private void setUpButtons() {
        binding.btnForgot.setRippleColor(getResources().getColor(R.color.deep_champagne));
        binding.btnKnow.setRippleColor(getResources().getColor(R.color.light_cyan));

        binding.btnForgot.setOnClickListener(v -> swipeInDirection(Direction.Left));
        binding.btnKnow.setOnClickListener(v -> swipeInDirection(Direction.Right));
        binding.btnFinish.setOnClickListener(v -> returnToPracticeTab());
    }

    /*
    Set up toolbar with a custom exit button.
    */
    private void setUpToolbar() {
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar.getRoot());
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("");
            }

            binding.toolbar.getRoot().setNavigationIcon(R.drawable.clear_icon);
            Drawable navigationIcon = binding.toolbar.getRoot().getNavigationIcon();
            if (navigationIcon != null) {
                navigationIcon.setTint(getResources().getColor(R.color.black));
            }
            binding.toolbar.getRoot().setNavigationOnClickListener(v -> returnToPracticeTab());
        }
    }

    /*
    Exit the practice session and return to the vocabulary view.
    */
    public void returnToPracticeTab() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new PracticeFragment())
                    .commit();
        }
    }

    /*
    Display a congratulatory message and exit button (called when all cards have been viewed).
    */
    private void finishSession() {
        binding.btnKnow.setVisibility(View.GONE);
        binding.btnForgot.setVisibility(View.GONE);
        binding.tvKnowHint.setVisibility(View.GONE);
        binding.tvForgotHint.setVisibility(View.GONE);
        binding.stackFlashcards.setVisibility(View.GONE);

        binding.tvFinished.setVisibility(View.VISIBLE);
        binding.btnFinish.setVisibility(View.VISIBLE);
    }

    /*
    Check whether the current card has been flipped and display a Toast if not.
    */
    private boolean checkIfCardFlipped() {
        if (!wasFlipped) {
            Toast.makeText(getActivity(), "You haven't flipped this card yet! 🤔", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*
    Swipe the current card in the given direction; fails if card has not been flipped yet.
    */
    private void swipeInDirection(Direction direction) {
        if (!checkIfCardFlipped()) return;

        SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                .setDirection(direction)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(new AccelerateInterpolator())
                .build();
        cardLayoutManager.setSwipeAnimationSetting(setting);
        binding.stackFlashcards.swipe();
    }

    /*
    Callbacks for card stack view.
    */
    @Override
    public void onCardSwiped(Direction direction) {
        int index = cardLayoutManager.getTopPosition() - 1;
        Word word = words.get(index);

        binding.stackFlashcards.setTranslationZ(0);
        binding.btnForgot.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        binding.btnKnow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));

        if (direction == Direction.Left) {
            adapter.add(word);
            word.decrementScore();
        } else {
            word.incrementScore();
        }
        adapter.remove(index);
        word.saveInBackground();

        if (adapter.getItemCount() == 0) {
            finishSession();
        }
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {
        // user shouldn't be able to swipe card until they've flipped it to look at the answer
        if (!wasFlipped) {
            // TODO: this doesn't work when swiping fast, seems to be a known issue:
            // https://github.com/yuyakaido/CardStackView/issues/306
            cardLayoutManager.setSwipeThreshold(1.0f);
        } else {
            cardLayoutManager.setSwipeThreshold(0.3f);
        }

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
    public void onCardCanceled() {
        checkIfCardFlipped();
        binding.stackFlashcards.setTranslationZ(0);
        binding.btnForgot.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        binding.btnKnow.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
    }

    @Override
    public void onCardAppeared(View view, int position) {
        Word word = words.get(position);
        word.setLastPracticed(new Date());
        word.saveInBackground();

        wasFlipped = false;
        FlipView flipview = view.findViewById(R.id.flipView);
        flipview.setOnFlippingListener((flipView, checked) -> wasFlipped = true);
    }

    @Override
    public void onCardDisappeared(View view, int position) {}

    @Override
    public void onCardRewound() {}
}