package com.example.lexis.models;

import com.google.common.primitives.Chars;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WordSearch {

    private char[][] grid;
    private List<WordSearchItem> wordItems;

    public WordSearch(String[] words) {
        int max = Math.max(getLongestWord(words), 8);
        grid = new char[max][max];
        wordItems = new ArrayList<>();
        for (char[] array : grid) {
            Arrays.fill(array, '.');
        }

        for (String word : words) {
            while (!placeWord(word)) {}
        }

        fillGrid();
        printGrid();
    }

    public char[][] getGrid() {
        return grid;
    }

    public char[] getFlatGrid() {
        return Chars.concat(grid);
    }

    public int getWidth() {
        return grid.length;
    }

    public int getHeight() {
        return grid[0].length;
    }

    public void printGrid() {
        for (char[] array : grid) {
            System.out.println(Arrays.toString(array));
        }
    }

    private boolean placeWord(String word) {
        int width = getWidth();
        int height = getHeight();

        String reverse = new StringBuffer(word).reverse().toString();
        String[] options = { word, reverse };
        Random random = new java.util.Random();
        int random_word = random.nextInt(options.length);
        word = options[random_word];

        int[][] directions = {{1, 0}, {0, 1}, {1, 1}};
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

        wordItems.add(new WordSearchItem(word, y, x, d));
        return true;
    }

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

    private int getLongestWord(String[] words) {
        int longest = words[0].length();
        for(int i = 1; i < words.length; i++) {
            if(words[i].length() > longest) {
                longest = words[i].length();
            }
        }
        return longest;
    }

    public class WordSearchItem {
        String word;
        GridLocation startLocation;
        GridLocation endLocation;

        public WordSearchItem(String word, int row, int col, int[] d) {
            this.word = word;
            this.startLocation = new GridLocation(row, col);
            GridLocation end = new GridLocation(row, col);
            if (d[0] == 1) end.col += word.length() - 1;
            if (d[1] == 1) end.row += word.length() - 1;
            this.endLocation = end;
        }
    }

    public class GridLocation {
        public int row;
        public int col;

        public GridLocation(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "(" + row +  ", " + col + ")";
        }
    }
}
