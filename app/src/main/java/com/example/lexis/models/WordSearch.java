package com.example.lexis.models;

import java.util.Arrays;
import java.util.Random;

public class WordSearch {

    char[][] grid;

    public WordSearch(String[] words) {
        grid = new char[8][8];
        for (char[] array : grid) {
            Arrays.fill(array, '.');
        }

        for (String word : words) {
            while (!placeWord(word)) {}
        }

        fillGrid();
        printGrid();
    }

    public void printGrid() {
        for (char[] array : grid) {
            System.out.println(Arrays.toString(array));
        }
    }

    public boolean placeWord(String word) {
        int width = grid.length;
        int height = grid[0].length;

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

        return true;
    }

    public void fillGrid() {
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
}
