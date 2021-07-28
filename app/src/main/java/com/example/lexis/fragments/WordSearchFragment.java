package com.example.lexis.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.R;
import com.example.lexis.adapters.WordSearchAdapter;
import com.example.lexis.databinding.FragmentWordSearchBinding;
import com.example.lexis.models.WordSearch;

public class WordSearchFragment extends Fragment {

    private static final String ARG_WORDS = "words";

    private FragmentWordSearchBinding binding;
    private char[] letters;
    private WordSearch wordSearch;

    public WordSearchFragment() {}

    public static WordSearchFragment newInstance(String[] words) {
        WordSearchFragment fragment = new WordSearchFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_WORDS, words);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String[] words = getArguments().getStringArray(ARG_WORDS);
            wordSearch = new WordSearch(words);
            letters = wordSearch.getFlatGrid();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWordSearchBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WordSearchAdapter adapter = new WordSearchAdapter(this, letters);
        binding.rvWordSearch.setLayoutManager(new GridLayoutManager(getActivity(), wordSearch.getWidth()));
        binding.rvWordSearch.setAdapter(adapter);

        setUpToolbar();
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
    private void returnToPracticeTab() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new PracticeFragment())
                    .commit();
        }
    }
}