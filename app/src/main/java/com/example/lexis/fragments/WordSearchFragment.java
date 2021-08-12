package com.example.lexis.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lexis.R;
import com.example.lexis.adapters.WordListAdapter;
import com.example.lexis.adapters.WordSearchAdapter;
import com.example.lexis.databinding.FragmentWordSearchBinding;
import com.example.lexis.models.Clue;
import com.example.lexis.models.GridLocation;
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

public class WordSearchFragment extends Fragment implements WordSearchHelpDialogFragment.WordSearchHelpDialogListener  {

    private static final String ARG_LANGUAGE = "targetLanguage";
    private static final String TAG = "WordSearchFragment";
    private static final int HELP_REQUEST_CODE = 145;
    private static final int DEFAULT_WORD_NUM = 6;
    private static final int DEFAULT_GRID_SIZE = 8;

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

    private GridLocation startLocation;
    private int startIndex;
    private DragDirection dragDirection;

    private long timeWhenPaused = 0;

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
            showProgressBar();
            fetchWords(targetLanguage);
        }

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
        query.addAscendingOrder(Word.KEY_WORD_LENGTH); // get short words first for easier display
        query.setLimit(DEFAULT_WORD_NUM);
        query.findInBackground((words, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting vocabulary", e);
                return;
            }
            hideProgressBar();
            createWordSearch(words);
            setUpRecyclerViews();
            startTimer();
        });
    }

    /*
    Generate a word search puzzle for the given list of words.
    */
    private void createWordSearch(List<Word> words) {
        int gridSize = DEFAULT_GRID_SIZE;
        int longestWord = Word.getLongestWord(words);
        if (longestWord >= gridSize) {
            gridSize = longestWord + 1;
        }
        wordSearch = new WordSearch(words, gridSize);
        this.words.addAll(words);
    }

    /*
    Set up the word search and word list recycler views.
    */
    private void setUpRecyclerViews() {
        // set up drag selection listener to highlight cells
        DragSelectionProcessor onDragSelectionListener = createOnDragSelectionListener();
        DragSelectTouchListener dragSelectTouchListener = new DragSelectTouchListener()
                .withSelectListener(onDragSelectionListener);
        binding.rvWordSearch.addOnItemTouchListener(dragSelectTouchListener);

        // set up word list recycler view
        wordListAdapter = new WordListAdapter(this, clues);
        binding.rvWordList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        binding.rvWordList.setAdapter(wordListAdapter);
        wordListAdapter.addAll(wordSearch.getClues());

        // set up word search grid recycler view
        wordSearchAdapter = new WordSearchAdapter(
                this, letters, targetLanguage, selectedPositions, currentlySelected, dragSelectTouchListener);
        wordSearchLayoutManager = new GridLayoutManager(getActivity(), wordSearch.getWidth());
        binding.rvWordSearch.setLayoutManager(wordSearchLayoutManager);
        binding.rvWordSearch.setAdapter(wordSearchAdapter);
        wordSearchAdapter.setLetters(wordSearch.getFlatGrid());
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

            // set up exit button
            binding.toolbar.getRoot().setNavigationIcon(R.drawable.clear_icon);
            Drawable navigationIcon = binding.toolbar.getRoot().getNavigationIcon();
            if (navigationIcon != null) {
                navigationIcon.setTint(getResources().getColor(R.color.black));
            }
            binding.toolbar.getRoot().setNavigationOnClickListener(v -> returnToPracticeTab());

            // set up language flag and help button
            binding.toolbar.tvFlag.setText(Utils.getFlagEmoji(targetLanguage));
            binding.toolbar.btnQuestion.setOnClickListener(v -> {
                pauseTimer();

                FragmentManager fm = activity.getSupportFragmentManager();
                WordSearchHelpDialogFragment dialog = WordSearchHelpDialogFragment.newInstance(targetLanguage);
                dialog.setTargetFragment(WordSearchFragment.this, HELP_REQUEST_CODE);
                dialog.show(fm, "fragment_word_search_help");
            });
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
                // starting a new drag action, store starting location
                if (calledFromOnStart) {
                    startIndex = start;
                    startLocation = getLocation(start);
                }

                // check if dragging upwards or to the left
                int current = end;
                if (start < startIndex) current = start;

                // update current drag direction
                GridLocation currentLocation = getLocation(current);
                if (currentLocation.col == startLocation.col) {
                    dragDirection = DragDirection.VERTICAL;
                } else if (currentLocation.row == startLocation.row) {
                    dragDirection = DragDirection.HORIZONTAL;
                } else {
                    dragDirection = DragDirection.NONE;
                }

                // update each valid cell with new selection state
                for (int i = start; i <= end; i++) {
                    currentLocation = getLocation(i);
                    if (!isValid(currentLocation.row, currentLocation.col)) continue;

                    if (isSelected) {
                        wordSearchAdapter.addCurrentlySelected(i);
                    } else {
                        wordSearchAdapter.removeCurrentlySelected(i);
                    }
                }
            }
        }).withMode(DragSelectionProcessor.Mode.Simple).withStartFinishedListener(createSelectionFinishedListener());
    }

    /*
    Create an ISelectionStartFinishedListener to check whether the highlighted word is valid and
    update the UI accordingly.
    */
    private DragSelectionProcessor.ISelectionStartFinishedListener createSelectionFinishedListener() {
        return new DragSelectionProcessor.ISelectionStartFinishedListener() {
            @Override
            public void onSelectionStarted(int start, boolean originalSelectionState) {}

            @Override
            public void onSelectionFinished(int end) {
                GridLocation endLocation = getLocation(end);

                // check whether a word exists between the start & end locations
                Pair<Boolean, String> hasWordBetween = wordSearch.hasWordBetween(
                        startLocation.row, startLocation.col, endLocation.row, endLocation.col);
                boolean wordExists = hasWordBetween.getLeft();
                String direction = hasWordBetween.getRight();

                // if dragging upwards or to the left, invert start and end indices
                int beginHighlight = startIndex;
                int endHighlight = end;
                if (end < startIndex) {
                    beginHighlight = end;
                    endHighlight = startIndex;
                }

                if (wordExists) {
                    // set selected cells as permanently highlighted
                    for (int i = beginHighlight; i <= endHighlight; i++) {
                        GridLocation currentLocation = getLocation(i);

                        if (isValid(currentLocation.row, currentLocation.col)) {
                            wordSearchAdapter.removeCurrentlySelected(i);
                            wordSearchAdapter.addSelected(i);
                        }
                    }

                    // get the word highlighted by the user
                    Word word;
                    if (direction.equals("regular")) {
                        word = wordSearch.getWordStartingAt(startLocation.row, startLocation.col);
                    } else {
                        word = wordSearch.getWordStartingAt(endLocation.row, endLocation.col);
                    }

                    // update data models to mark word as found
                    int index = words.indexOf(word);
                    Clue clue = clues.get(index);
                    clue.setFound(true);
                    numCluesFound++;
                    wordListAdapter.notifyItemChanged(index);

                    // save last practiced time
                    word.setLastPracticed(new Date());
                    word.saveInBackground();

                    // finish puzzle if all clues are found
                    if (numCluesFound == clues.size()) {
                        pauseTimer();
                        String time = binding.toolbar.timer.getText().toString();
                        long millis = Utils.timerStringToMillis(time);
                        boolean isNewBest = Utils.isPersonalBest(millis, ParseUser.getCurrentUser());
                        if (millis != 0 && isNewBest) {
                            Utils.updateBestTime(millis, ParseUser.getCurrentUser());
                        }

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> finishWordPuzzle(time, isNewBest), 500);
                    }
                } else { // not a valid word, automatically de-select
                    for (int i = beginHighlight; i <= endHighlight; i++) {
                        GridLocation currentLocation = getLocation(i);

                        if (isValid(currentLocation.row, currentLocation.col)) {
                            wordSearchAdapter.removeCurrentlySelected(i);
                        }
                    }
                }
            }
        };
    }

    /*
    Check whether the move to the current row & column is valid given the direction of movement so far.
    */
    private boolean isValid(int currentRow, int currentCol) {
        if (startLocation.col != currentCol && dragDirection == DragDirection.VERTICAL) return false;
        if (startLocation.row != currentRow && dragDirection == DragDirection.HORIZONTAL) return false;
        return dragDirection != DragDirection.NONE;
    }

    /*
    Return a GridLocation for the cell at the specified index.
    */
    private GridLocation getLocation(int index) {
        int currentRow = index / wordSearch.getWidth();
        int currentCol = index % wordSearch.getWidth();
        return new GridLocation(currentRow, currentCol);
    }

    /*
    Finish the word puzzle and display a congratulatory message + time taken to solve the puzzle.
    */
    private void finishWordPuzzle(String time, boolean newBest) {
        binding.rvWordSearch.setVisibility(View.GONE);
        binding.rvWordList.setVisibility(View.GONE);
        binding.toolbar.tvFlag.setVisibility(View.GONE);
        binding.toolbar.timer.setVisibility(View.GONE);
        binding.toolbar.btnQuestion.setVisibility(View.GONE);
        if (newBest) {
            binding.tvPersonalBest.setText(R.string.personal_best);
        } else {
            String best = Utils.millisToTimerString(Utils.getBestTime(ParseUser.getCurrentUser()));
            binding.tvPersonalBest.setText(String.format("Your personal best time is: %s. Can you beat it? 🎯", best));
        }
        binding.layoutFinished.setVisibility(View.VISIBLE);
        binding.tvFinished.setText(String.format(getString(R.string.word_search_congratulations), time));
    }

    /*
    Resume timer when exiting out of help dialog.
    */
    @Override
    public void onFinishDialog() {
        startTimer();
    }

    /*
    Helper functions to show and hide progress bar while puzzle loads.
    */
    private void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
    }

    /*
    Helper functions to pause and resume timer.
    */
    private void startTimer() {
        binding.toolbar.timer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
        binding.toolbar.timer.start();
    }

    private void pauseTimer() {
        timeWhenPaused = binding.toolbar.timer.getBase() - SystemClock.elapsedRealtime();
        binding.toolbar.timer.stop();
    }
}