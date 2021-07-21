package com.example.lexis.fragments;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lexis.R;
import com.example.lexis.adapters.VocabularyAdapter;
import com.example.lexis.databinding.FragmentPracticeBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.RoundedHighlightSpan;
import com.example.lexis.utilities.Utils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class PracticeFragment extends Fragment implements VocabularyFilterDialogFragment.VocabularyFilterDialogListener {

    private static final String TAG = "PracticeFragment";
    FragmentPracticeBinding binding;
    List<Word> vocabulary;
    VocabularyAdapter adapter;
    ArrayList<String> selectedLanguages;

    public PracticeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPracticeBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i(TAG, "onViewCreated");

        selectedLanguages = new ArrayList<>(Utils.getCurrentStudiedLanguages());
        vocabulary = new ArrayList<>();
        queryVocabulary(selectedLanguages);

        // set up recyclerView
        adapter = new VocabularyAdapter(this, vocabulary);
        binding.rvVocabulary.setAdapter(adapter);
        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(getActivity()));

        // set up pull to refresh
        binding.swipeContainer.setOnRefreshListener(() -> {
            adapter.clear();
            queryVocabulary(selectedLanguages);
            binding.swipeContainer.setRefreshing(false);
        });
        binding.swipeContainer.setColorSchemeResources(R.color.tiffany_blue,
                R.color.light_cyan,
                R.color.orange_peel,
                R.color.mellow_apricot);

        // set up toolbar
        Utils.setLanguageLogo(binding.toolbar.ivLogo);
        binding.toolbar.ibFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null) {
                    FragmentManager fm = activity.getSupportFragmentManager();
                    ArrayList<String> languageOptions = new ArrayList<>(Utils.getCurrentStudiedLanguages());
                    VocabularyFilterDialogFragment dialog = VocabularyFilterDialogFragment.newInstance(languageOptions, selectedLanguages);
                    dialog.setTargetFragment(PracticeFragment.this, 124);
                    dialog.show(fm, "fragment_vocabulary_filter");
                }
            }
        });

        styleEmptyVocabularyPrompt();
    }

    private void queryVocabulary(ArrayList<String> languages) {
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereContainedIn(Word.KEY_TARGET_LANGUAGE, languages);
        query.addDescendingOrder("createdAt");
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary", e);
                return;
            }
            adapter.addAll(words);
            checkVocabularyEmpty(words);
        });
    }

    private void styleEmptyVocabularyPrompt() {
        String prompt = getString(R.string.empty_vocabulary_prompt);
        int start = prompt.indexOf("highlighted");
        int end = start + "highlighted".length();

        SpannableString styledPrompt = new SpannableString(prompt);
        styledPrompt.setSpan(new RoundedHighlightSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvEmptyPrompt.setText(styledPrompt);
    }

    /*
    Check if the user's vocabulary is empty, and display a prompting message if it is.
    */
    private void checkVocabularyEmpty(List<Word> words) {
        if (words.isEmpty()) {
            binding.tvEmptyPrompt.setVisibility(View.VISIBLE);
            binding.rvVocabulary.setVisibility(View.INVISIBLE);
            binding.btnPractice.setVisibility(View.INVISIBLE);
            binding.toolbar.ibFilter.setVisibility(View.INVISIBLE);
        } else {
            binding.tvEmptyPrompt.setVisibility(View.INVISIBLE);
            binding.rvVocabulary.setVisibility(View.VISIBLE);
            binding.btnPractice.setVisibility(View.VISIBLE);
            binding.toolbar.ibFilter.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFinishDialog(ArrayList<String> selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
        adapter.clear();
        queryVocabulary(selectedLanguages);
    }
}