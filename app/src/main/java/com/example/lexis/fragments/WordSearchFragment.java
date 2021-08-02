package com.example.lexis.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.lexis.R;
import com.example.lexis.adapters.WordListAdapter;
import com.example.lexis.adapters.WordSearchAdapter;
import com.example.lexis.databinding.FragmentWordSearchBinding;
import com.example.lexis.models.Clue;
import com.example.lexis.models.Word;
import com.example.lexis.models.WordSearch;
import com.example.lexis.utilities.Utils;
import com.michaelflisar.dragselectrecyclerview.DragSelectTouchListener;
import com.michaelflisar.dragselectrecyclerview.DragSelectionProcessor;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordSearchFragment extends Fragment {

    private static final String ARG_LANGUAGE = "targetLanguage";
    private static final String TAG = "WordSearchFragment";
    public static final int DEFAULT_WORD_NUM = 6;
    public static final int DEFAULT_GRID_SIZE = 8;

    private FragmentWordSearchBinding binding;
    private String targetLanguage;
    private List<Word> words;
    private WordSearch wordSearch;

    private WordSearchAdapter wordSearchAdapter;
    private GridLayoutManager wordSearchLayoutManager;
    private WordListAdapter wordListAdapter;
    private char[] letters;
    private Set<Integer> selectedPositions;
    private Set<Integer> currentlySelected;
    private List<Clue> clues;
    private int numCluesFound;

    private int startCol;
    private int startRow;
    private int startIndex;
    private DragDirection dragDirection;

    public WordSearchFragment() {}

    public static WordSearchFragment newInstance(String targetLanguage) {
        WordSearchFragment fragment = new WordSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LANGUAGE, targetLanguage);
        fragment.setArguments(args);
        return fragment;
    }

    private enum DragDirection {
        HORIZONTAL, VERTICAL, NONE
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetLanguage = getArguments().getString(ARG_LANGUAGE);
            selectedPositions = new HashSet<>();
            currentlySelected = new HashSet<>();
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

        if (words == null) {
            words = new ArrayList<>();
            clues = new ArrayList<>();
            letters = new char[]{};
            numCluesFound = 0;
            fetchWords(targetLanguage);
        }

        wordListAdapter = new WordListAdapter(this, clues);
        binding.rvWordList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        binding.rvWordList.setAdapter(wordListAdapter);

        binding.btnFinish.setOnClickListener(v -> returnToPracticeTab());
        setUpToolbar();
    }

    /*
    Fetch words for the word search and add them to the respective adapters.
    */
    private void fetchWords(String targetLanguage) {
        ParseQuery<Word> query = ParseQuery.getQuery(Word.class);
        query.include(Word.KEY_USER);
        query.whereEqualTo(Word.KEY_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(Word.KEY_TARGET_LANGUAGE, targetLanguage);
        query.orderByAscending(Word.KEY_SCORE);
        query.addAscendingOrder(Word.KEY_LAST_PRACTICED);
        // get short words first for easier display
        query.addAscendingOrder(Word.KEY_WORD_LENGTH);
        query.setLimit(DEFAULT_WORD_NUM);
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary", e);
                return;
            }

            int gridSize = DEFAULT_GRID_SIZE;
            int longestWord = Word.getLongestWord(words);
            if (longestWord >= gridSize) {
                gridSize = longestWord + 1;
            }
            wordSearch = new WordSearch(words, gridSize);
            this.words.addAll(words);

            // set up drag selection listener
            DragSelectionProcessor onDragSelectionListener = createOnDragSelectionListener();
            DragSelectTouchListener dragSelectTouchListener = new DragSelectTouchListener()
                    .withSelectListener(onDragSelectionListener);
            binding.rvWordSearch.addOnItemTouchListener(dragSelectTouchListener);

            wordSearchAdapter = new WordSearchAdapter(
                    this, letters, selectedPositions, currentlySelected, dragSelectTouchListener);
            wordSearchLayoutManager = new GridLayoutManager(getActivity(), wordSearch.getWidth());
            binding.rvWordSearch.setLayoutManager(wordSearchLayoutManager);
            binding.rvWordSearch.setAdapter(wordSearchAdapter);

            wordSearchAdapter.setLetters(wordSearch.getFlatGrid());
            wordListAdapter.addAll(wordSearch.getClues());
        });
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

            binding.toolbar.tvFlag.setText(Utils.getFlagEmoji(targetLanguage));
            binding.toolbar.tvFlag.setOnClickListener(v -> {
                String hint = String.format("Look for %s translations of the word search clues!", Utils.getSpinnerText(targetLanguage));
                Toast.makeText(getActivity(), hint, Toast.LENGTH_SHORT).show();
            });
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

    /*
    Create an onDragSelectionListener to highlight cells upon dragging.
    */
    private DragSelectionProcessor createOnDragSelectionListener() {
        return new DragSelectionProcessor(new DragSelectionProcessor.ISelectionHandler() {
            @Override
            public Set<Integer> getSelection() {
                return selectedPositions;
            }

            @Override
            public boolean isSelected(int index) {
                return selectedPositions.contains(index);
            }

            @Override
            public void updateSelection(int start, int end, boolean isSelected, boolean calledFromOnStart) {
                if (calledFromOnStart) {
                    startIndex = start;
                    startCol = start % wordSearch.getWidth();
                    startRow = start / wordSearch.getWidth();
                }

                int current = end;
                if (start < startIndex) { // dragging vertically upwards
                    current = start;
                }

                // TODO: reset previous cells if switching direction mid-drag
                int currentCol = current % wordSearch.getWidth();
                int currentRow = current / wordSearch.getWidth();
                if (currentCol == startCol) {
                    dragDirection = DragDirection.VERTICAL;
                } else if (currentRow == startRow) {
                    dragDirection = DragDirection.HORIZONTAL;
                } else {
                    dragDirection = DragDirection.NONE;
                }

                for (int i = start; i <= end; i++) {
                    currentCol = i % wordSearch.getWidth();
                    currentRow = i / wordSearch.getWidth();

                    if (startCol != currentCol && dragDirection == DragDirection.VERTICAL) continue;
                    if (startRow != currentRow && dragDirection == DragDirection.HORIZONTAL) continue;
                    if (dragDirection == DragDirection.NONE) continue;

                    if (isSelected) {
                        wordSearchAdapter.addCurrentlySelected(i);
                        // selectedPositions.add(i);
                    } else {
                        wordSearchAdapter.removeCurrentlySelected(i);
                        // selectedPositions.remove(i);
                    }
                    // wordSearchAdapter.notifyItemChanged(i);
                }
            }
        }).withMode(DragSelectionProcessor.Mode.Simple).withStartFinishedListener(new DragSelectionProcessor.ISelectionStartFinishedListener() {
            @Override
            public void onSelectionStarted(int start, boolean originalSelectionState) {
                Log.i(TAG, "started dragging");
            }

            @Override
            public void onSelectionFinished(int end) {
                int endCol = end % wordSearch.getWidth();
                int endRow = end / wordSearch.getWidth();

                Pair<Boolean, String> hasWordBetween = wordSearch.hasWordBetween(
                        startRow, startCol, endRow, endCol);
                boolean wordExists = hasWordBetween.getLeft();
                String direction = hasWordBetween.getRight();

                if (wordExists) {
                    int beginHighlight = startIndex;
                    int endHighlight = end;

                    if (end < startIndex) {
                        beginHighlight = end;
                        endHighlight = startIndex;
                    }

                    for (int i = beginHighlight; i <= endHighlight; i++) {
                        int currentCol = i % wordSearch.getWidth();
                        int currentRow = i / wordSearch.getWidth();

                        if (isValid(currentRow, currentCol)) {
                            wordSearchAdapter.removeCurrentlySelected(i);
                            wordSearchAdapter.addSelected(i);
                        }
                    }

                    Word word;
                    if (direction.equals("regular")) {
                        word = wordSearch.getWordStartingAt(startRow, startCol);
                    } else {
                        word = wordSearch.getWordStartingAt(endRow, endCol);
                    }

                    int index = words.indexOf(word);
                    Clue clue = clues.get(index);
                    clue.setFound(true);
                    numCluesFound++;
                    wordListAdapter.notifyItemChanged(index);

                    // save last practiced time
                    word.setLastPracticed(new Date());
                    word.saveInBackground();

                    if (numCluesFound == clues.size()) {
                        // display congratulations message after 0.5 seconds
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            binding.rvWordSearch.setVisibility(View.GONE);
                            binding.rvWordList.setVisibility(View.GONE);
                            binding.toolbar.tvFlag.setVisibility(View.GONE);
                            binding.layoutFinished.setVisibility(View.VISIBLE);
                        }, 500);
                    }

                    Log.i(TAG, "found word: " + word.getTargetWord());
                    Log.i(TAG, "relevant clue: " + clue.getText());
                } else { // not a valid word, automatically de-select
                    // TODO: lots of repeated code, need to refactor
                    int beginHighlight = startIndex;
                    int endHighlight = end;

                    if (end < startIndex) {
                        beginHighlight = end;
                        endHighlight = startIndex;
                    }

                    for (int i = beginHighlight; i <= endHighlight; i++) {
                        int currentCol = i % wordSearch.getWidth();
                        int currentRow = i / wordSearch.getWidth();

                        if (isValid(currentRow, currentCol)) {
                            wordSearchAdapter.removeCurrentlySelected(i);
                        }
                    }
                }
            }
        });
    }

    // TODO: add comment here
    private boolean isValid(int currentRow, int currentCol) {
        if (startCol != currentCol && dragDirection == DragDirection.VERTICAL) return false;
        if (startRow != currentRow && dragDirection == DragDirection.HORIZONTAL) return false;
        if (dragDirection == DragDirection.NONE) return false;
        return true;
    }
}