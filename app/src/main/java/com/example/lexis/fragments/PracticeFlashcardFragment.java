package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.adapters.FlashcardsAdapter;
import com.example.lexis.databinding.FragmentPracticeFlashcardBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Utils;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;

import java.util.ArrayList;
import java.util.List;

public class PracticeFlashcardFragment extends Fragment implements CardStackListener {

    private static final String TAG = "FlashcardFragment";

    FragmentPracticeFlashcardBinding binding;
    List<Word> words;
    FlashcardsAdapter adapter;
    CardStackLayoutManager cardLayoutManager;

    public PracticeFlashcardFragment() {
        // Required empty public constructor
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
            fetchVocabulary();
        }

        // set up card stack view
        adapter = new FlashcardsAdapter(this, words);
        cardLayoutManager = new CardStackLayoutManager(getActivity(), this);
        cardLayoutManager.setStackFrom(StackFrom.TopAndRight);
        cardLayoutManager.setTranslationInterval(12.0f);

        binding.stackFlashcards.setLayoutManager(cardLayoutManager);
        binding.stackFlashcards.setAdapter(adapter);

        // set up toolbar with custom back button
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        if (activity != null) {
            activity.setSupportActionBar(binding.toolbar.getRoot());
            activity.getSupportActionBar().setTitle("");
            binding.toolbar.getRoot().setNavigationIcon(R.drawable.back_arrow);
            binding.toolbar.getRoot().getNavigationIcon().setTint(getResources().getColor(R.color.black));
            binding.toolbar.getRoot().setNavigationOnClickListener(v -> activity.onBackPressed());
        }
    }

    private void fetchVocabulary() {
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Word.KEY_TARGET_LANGUAGE, Utils.getCurrentTargetLanguage());
        query.addDescendingOrder("createdAt");
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary", e);
                return;
            }
            adapter.addAll(words);
        });
    }

    @Override
    public void onCardSwiped(Direction direction) {
        if (direction == Direction.Left) {
            adapter.add(words.get(cardLayoutManager.getTopPosition() - 1));
            Toast.makeText(getActivity(), "I don't know this card! ❌", Toast.LENGTH_SHORT).show();
        } else if (direction == Direction.Right) {
            Toast.makeText(getActivity(), "I know this card! ✅", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {}


    @Override
    public void onCardRewound() {}

    @Override
    public void onCardCanceled() {}

    @Override
    public void onCardAppeared(View view, int position) {}

    @Override
    public void onCardDisappeared(View view, int position) {}
}