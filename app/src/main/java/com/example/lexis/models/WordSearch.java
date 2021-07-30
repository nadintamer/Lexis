package com.example.lexis.models;

import com.google.common.primitives.Chars;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WordSearch {

    private char[][] grid;
    private List<WordSearchItem> wordItems;

    /*
    Construct a new WordSearch object from the given word list by generating the grid.
    */
    public WordSearch(List<Word> words, int size) {
        grid = new char[size][size];
        wordItems = new ArrayList<>();
        for (char[] array : grid) {
            Arrays.fill(array, '.');
        }

        for (int i = 0; i < words.size(); i++) {
            // attempt to place the word until we succeed
            while (!placeWord(words.get(i))) {}
        }

        fillGrid();
        printGrid();
    }

    public char[][] getGrid() {
        return grid;
    }

    /*
    Return a flattened, 1-D version of the word search grid.
    */
    public char[] getFlatGrid() {
        return Chars.concat(grid);
    }

    public int getWidth() {
        return grid.length;
    }

    public int getHeight() {
        return grid[0].length;
    }

    /*
    Return a list of the clues (i.e. English words) for the word search puzzle.
    */
    public List<String> getClues() {
        List<String> clues = new ArrayList<>();
        for (WordSearchItem item : wordItems) {
            clues.add(item.word.getEnglishWord());
        }
        return clues;
    }

    public void printGrid() {
        for (char[] array : grid) {
            System.out.println(Arrays.toString(array));
        }
    }

    /*
    Attempt to place the given word somewhere within the word search grid without overlapping any
    incorrect letters. Return true if successful, false otherwise.
    */
    private boolean placeWord(Word wordObject) {
        int width = getWidth();
        int height = getHeight();

        String word = wordObject.getTargetWord();
        String reverse = new StringBuffer(word).reverse().toString();
        String[] options = { word, reverse };
        Random random = new java.util.Random();
        int random_word = random.nextInt(options.length);
        word = options[random_word];

        int[][] directions = {{1, 0}, {0, 1}};
        int random_direction = random.nextInt(directions.length);
        int[] d = directions[random_direction];

        int xsize = d[0] == 0 ? width : width - word.length();
        int ysize = d[1] == 0 ? height : height - word.length();

        int x = random.nextInt(xsize);
        int y = random.nextInt(ysize);

        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            char gridLetter = grid[y + d[1] * i][x + d[0] * i];
            if (gridLetter != '.' && gridLetter != letter) {
                return false;
            }
        }

        for (int i = 0; i < word.length(); i++) {
            char letter = word.charAt(i);
            grid[y + d[1] * i][x + d[0] * i] = letter;
        }

        wordItems.add(new WordSearchItem(wordObject, y, x, d));
        return true;
    }

    /*
    Fill the remaining empty cells in the grid with random characters.
    */
    private void fillGrid() {
        Random random = new java.util.Random();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == '.') {
                    char c = (char) (random.nextInt(26) + 'a');
                    grid[row][col] = c;
                }
            }
        }
    }

    /*
    Check whether the grid has a word starting at the specified row and column.
    */
    public boolean hasWordStartingAt(int row, int col) {
        for (WordSearchItem item : wordItems) {
            if (item.startLocation.row == row && item.startLocation.col == col) {
                return true;
            }
        }
        return false;
    }

    /*
    Check whether the grid has a word ending at the specified row and column.
    */
    public boolean hasWordEndingAt(int row, int col) {
        for (WordSearchItem item : wordItems) {
            if (item.endLocation.row == row && item.endLocation.col == col) {
                return true;
            }
        }
        return false;
    }

    /*
    Check whether the grid has a word between the specified start and end locations (accounts for
    left and upwards dragging, in which the start and end positions will be reversed).
    */
    public Pair<Boolean, String> hasWordBetween(int startRow, int startCol, int endRow, int endCol) {
        if (hasWordStartingAt(startRow, startCol) && hasWordEndingAt(endRow, endCol)) {
            return new ImmutablePair<>(true, "regular");
        } else if (hasWordStartingAt(endRow, endCol) && hasWordEndingAt(startRow, startCol)) {
            return new ImmutablePair<>(true, "reversed");
        }
        return new ImmutablePair<>(false, "");
    }

    /*
    Get the word starting at the specified row and column.
    */
    public Word getWordStartingAt(int row, int col) {
        for (WordSearchItem item : wordItems) {
            if (item.startLocation.row == row && item.startLocation.col == col) {
                return item.word;
            }
        }
        return null;
    }

    /*
    Represents a single word within the word search, along with its start and end location, as
    well as direction.
    */
    public class WordSearchItem {
        Word word;
        GridLocation startLocation;
        GridLocation endLocation;
        int[] direction;

        public WordSearchItem(Word word, int row, int col, int[] d) {
            this.word = word;
            this.startLocation = new GridLocation(row, col);
            GridLocation end = new GridLocation(row, col);
            if (d[0] == 1) end.col += word.getTargetWord().length() - 1;
            if (d[1] == 1) end.row += word.getTargetWord().length() - 1;
            this.endLocation = end;
            this.direction = d;
        }
    }

    /*
    Represents a single row-column location within the word search grid.
    */
    public class GridLocation {
        public int row;
        public int col;

        public GridLocation(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @NotNull
        @Override
        public String toString() {
            return "(" + row +  ", " + col + ")";
        }
    }
}
