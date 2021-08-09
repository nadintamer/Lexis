package com.example.lexis.fragments;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.R;
import com.example.lexis.fragments.VocabularyFilterDialogFragment.Sort;
import com.example.lexis.adapters.VocabularyAdapter;
import com.example.lexis.databinding.FragmentPracticeBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.RoundedHighlightSpan;
import com.example.lexis.utilities.SwipeDeleteCallback;
import com.example.lexis.utilities.Utils;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class PracticeFragment extends Fragment implements VocabularyFilterDialogFragment.VocabularyFilterDialogListener, VocabularyNewDialogFragment.VocabularyNewDialogListener{

    private static final String TAG = "PracticeFragment";
    private static final int VOCABULARY_FILTER_REQUEST_CODE = 124;
    private static final int VOCABULARY_NEW_REQUEST_CODE = 342;

    public FragmentPracticeBinding binding;
    List<Word> vocabulary;
    VocabularyAdapter adapter;
    ArrayList<String> selectedLanguages;
    boolean starredOnly;
    Sort sortBy;

    public PracticeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPracticeBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resetVocabularyFilters();
        vocabulary = new ArrayList<>();
        queryVocabulary();

        setUpRecyclerView();
        setUpToolbar();
        setUpPracticeButton();
        setUpSearchBar();
    }

    /*
    Set up the recyclerView for displaying vocabulary.
    */
    private void setUpRecyclerView() {
        adapter = new VocabularyAdapter(this, vocabulary);
        binding.rvVocabulary.setAdapter(adapter);
        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(getActivity()));

        // set up pull to refresh
        binding.swipeContainer.setOnRefreshListener(() -> {
            adapter.clear();
            resetVocabularyFilters();
            queryVocabulary();
            binding.swipeContainer.setRefreshing(false);
        });
        binding.swipeContainer.setColorSchemeResources(R.color.tiffany_blue,
                R.color.light_cyan,
                R.color.orange_peel,
                R.color.mellow_apricot);

        // set up swipe left to delete
        SwipeDeleteCallback callback = new SwipeDeleteCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(binding.rvVocabulary);

        // hide practice button if on last item so that user can see star / flag
        binding.rvVocabulary.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvVocabulary.getLayoutManager();
                if (layoutManager != null) {
                    int position = layoutManager.findLastCompletelyVisibleItemPosition();
                    View lastVisibleView = layoutManager.findViewByPosition(position);
                    int lastItemIndex = binding.rvVocabulary.getAdapter().getItemCount() - 1;
                    if (position != -1 && position == lastItemIndex) {
                        if (Utils.viewsOverlap(lastVisibleView, binding.btnPractice)) {
                            binding.btnPractice.hide();
                        }
                    } else {
                        binding.btnPractice.show();
                    }
                }
            }
        });
    }

    /*
    Set up the toolbar logo and button.
    */
    private void setUpToolbar() {
        Utils.setLanguageLogo(binding.toolbar.ivLogo);
        binding.toolbar.ibFilter.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                FragmentManager fm = activity.getSupportFragmentManager();
                ArrayList<String> languageOptions = new ArrayList<>(Utils.getCurrentStudiedLanguages());
                VocabularyFilterDialogFragment dialog = VocabularyFilterDialogFragment.newInstance(
                        languageOptions, selectedLanguages, starredOnly, sortBy);
                dialog.setTargetFragment(PracticeFragment.this, VOCABULARY_FILTER_REQUEST_CODE);
                dialog.show(fm, "fragment_vocabulary_filter");
            }
        });
        binding.toolbar.ibAddNew.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                FragmentManager fm = activity.getSupportFragmentManager();
                VocabularyNewDialogFragment dialog = new VocabularyNewDialogFragment();
                dialog.setTargetFragment(PracticeFragment.this, VOCABULARY_NEW_REQUEST_CODE);
                dialog.show(fm, "fragment_vocabulary_new");
            }
        });
    }

    /*
    Set up button for navigating to practice session.
    */
    private void setUpPracticeButton() {
        binding.btnPractice.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, new PracticeIntroFragment())
                        .addToBackStack(null) // add to back stack so we can return to this fragment
                        .commit();
            }
        });
    }

    /*
    Set up the vocabulary search bar.
    */
    private void setUpSearchBar() {
        binding.searchBar.setIconifiedByDefault(true);
        binding.searchBar.setOnClickListener(v -> binding.searchBar.onActionViewExpanded());
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchVocabulary(query);
                binding.searchBar.clearFocus();
                binding.searchBar.setQuery("", false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchVocabulary(newText);
                return false;
            }
        });

        ImageView clearButton = binding.searchBar.findViewById(androidx.appcompat.R.id.search_close_btn);
        clearButton.setOnClickListener(v -> {
            if (binding.searchBar.getQuery().length() == 0) {
                binding.searchBar.setIconified(true);
            } else {
                binding.searchBar.setQuery("", false);
                binding.searchBar.clearFocus();
                adapter.clear();
                queryVocabulary();
            }
        });
    }

    /*
    Fetch the user's vocabulary for the given languages.
    */
    private void queryVocabulary() {
        showProgressBar();
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereContainedIn(Word.KEY_TARGET_LANGUAGE, selectedLanguages);
        if (starredOnly) {
            query.whereEqualTo(Word.KEY_STARRED, true);
        }
        if (sortBy == Sort.ALPHABETICALLY) {
            query.addAscendingOrder(Word.KEY_TARGET_WORD_SEARCH); // sort alphabetically
        } else {
            query.addDescendingOrder(Word.KEY_CREATED_AT); // sort by date added
        }
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary", e);
                return;
            }
            hideProgressBar();
            adapter.addAll(words);
            checkVocabularyEmpty(words);
        });
    }

    /*
    Search the user's vocabulary for the given query.
    */
    private void searchVocabulary(String searchQuery) {
        showProgressBar();
        searchQuery = searchQuery.toLowerCase();
        ParseQuery<Word> targetWord = ParseQuery.getQuery(Word.class);
        targetWord.whereStartsWith(Word.KEY_TARGET_WORD_SEARCH, searchQuery);

        ParseQuery<Word> englishWord = ParseQuery.getQuery(Word.class);
        englishWord.whereStartsWith(Word.KEY_ENGLISH_WORD_SEARCH, searchQuery);

        List<ParseQuery<Word>> queries = new ArrayList<>();
        queries.add(targetWord);
        queries.add(englishWord);

        // get words where either target word OR english word starts with search query
        ParseQuery<Word> query = ParseQuery.or(queries);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereContainedIn(Word.KEY_TARGET_LANGUAGE, selectedLanguages);
        if (starredOnly) {
            query.whereEqualTo(Word.KEY_STARRED, true);
        }
        if (sortBy == Sort.ALPHABETICALLY) {
            query.addAscendingOrder(Word.KEY_TARGET_WORD_SEARCH); // sort alphabetically
        } else {
            query.addDescendingOrder(Word.KEY_CREATED_AT); // sort by date added
        }
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary", e);
                return;
            }
            hideProgressBar();
            adapter.clear();
            adapter.addAll(words);
            checkSearchEmpty(words);
        });
    }

    /*
    Style the prompt shown when vocabulary is empty.
    */
    private void styleEmptyVocabularyPrompt() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            String prompt = activity.getString(R.string.empty_vocabulary_prompt);
            int start = prompt.indexOf("highlighted");
            int end = start + "highlighted".length();

            SpannableString styledPrompt = new SpannableString(prompt);
            styledPrompt.setSpan(new RoundedHighlightSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.tvEmptyPrompt.setText(styledPrompt);
        }
    }

    /*
    Check if the user's vocabulary is empty, and display a prompting message if it is.
    */
    private void checkSearchEmpty(List<Word> words) {
        binding.tvEmptyPrompt.setText(R.string.no_search_results);
        if (words.isEmpty()) {
            binding.tvEmptyPrompt.setVisibility(View.VISIBLE);
            binding.rvVocabulary.setVisibility(View.INVISIBLE);
        } else {
            binding.tvEmptyPrompt.setVisibility(View.INVISIBLE);
            binding.rvVocabulary.setVisibility(View.VISIBLE);
        }
    }

    /*
    Check if the user's vocabulary is empty, and display a prompting message if it is.
    */
    public void checkVocabularyEmpty(List<Word> words) {
        styleEmptyVocabularyPrompt();
        if (words.isEmpty()) {
            binding.tvEmptyPrompt.setVisibility(View.VISIBLE);
            binding.rvVocabulary.setVisibility(View.INVISIBLE);
            binding.btnPractice.setVisibility(View.INVISIBLE);
            binding.searchBar.setVisibility(View.INVISIBLE);
            binding.toolbar.ibFilter.setVisibility(View.INVISIBLE);
        } else {
            binding.tvEmptyPrompt.setVisibility(View.INVISIBLE);
            binding.rvVocabulary.setVisibility(View.VISIBLE);
            binding.btnPractice.setVisibility(View.VISIBLE);
            binding.searchBar.setVisibility(View.VISIBLE);
            binding.toolbar.ibFilter.setVisibility(View.VISIBLE);
        }
    }

    /*
    Reset vocabulary filters back to default values.
    */
    private void resetVocabularyFilters() {
        selectedLanguages = new ArrayList<>(Utils.getCurrentStudiedLanguages());
        starredOnly = false;
        sortBy = Sort.DATE;
    }

    /*
    Called after vocabulary filter dialog is dismissed; filter vocabulary based on languages selected.
    */
    @Override
    public void onFinishDialog(ArrayList<String> selectedLanguages, boolean starredOnly, Sort sortBy) {
        this.selectedLanguages = selectedLanguages;
        this.starredOnly = starredOnly;
        this.sortBy = sortBy;

        adapter.clear();
        queryVocabulary();
    }

    /*
    Called after new vocabulary dialog is dismissed; add new word to vocabulary.
    */
    @Override
    public void onFinishDialog(String targetLanguage, String targetWord, String englishWord) {
        Utils.addWordToDatabase(targetLanguage, targetWord, englishWord, binding.rvVocabulary);
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