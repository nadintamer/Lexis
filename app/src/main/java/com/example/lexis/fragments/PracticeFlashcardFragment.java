package com.example.lexis.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.adapters.ArticlesAdapter;
import com.example.lexis.adapters.FlashcardsAdapter;
import com.example.lexis.databinding.FragmentPracticeFlashcardBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Utils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class PracticeFlashcardFragment extends Fragment {

    private static final String TAG = "FlashcardFragment";

    FragmentPracticeFlashcardBinding binding;
    List<Word> words;
    FlashcardsAdapter adapter;

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

        // set up recyclerView
        adapter = new FlashcardsAdapter(this, words);
        binding.rvFlashcards.setAdapter(adapter);
        binding.rvFlashcards.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(binding.rvFlashcards);
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
}