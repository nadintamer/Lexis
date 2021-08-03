package com.example.lexis.models;

import org.jetbrains.annotations.NotNull;

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
