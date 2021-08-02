package com.example.lexis.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.R;
import com.example.lexis.databinding.ItemWordSearchBinding;
import com.michaelflisar.dragselectrecyclerview.DragSelectTouchListener;

import java.util.Set;

public class WordSearchAdapter extends RecyclerView.Adapter<WordSearchAdapter.WordSearchViewHolder> {

    char[] letters;
    Set<Integer> selectedPositions;
    Set<Integer> currentlySelected;
    String targetLanguage;
    Fragment fragment;
    DragSelectTouchListener dragSelectTouchListener;

    public WordSearchAdapter(Fragment fragment, char[] letters, String targetLanguage, Set<Integer> selectedPositions, Set<Integer> currentlySelected, DragSelectTouchListener dragSelectTouchListener) {
        this.fragment = fragment;
        this.letters = letters;
        this.targetLanguage = targetLanguage;
        this.dragSelectTouchListener = dragSelectTouchListener;
        this.selectedPositions = selectedPositions;
        this.currentlySelected = currentlySelected;
    }

    @NonNull
    @Override
    public WordSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordSearchBinding binding = ItemWordSearchBinding.inflate(LayoutInflater.from(fragment.getActivity()), parent, false);
        return new WordSearchViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordSearchViewHolder holder, int position) {
        char letter = letters[position];
        holder.bind(letter, position);
    }

    @Override
    public int getItemCount() {
        return letters.length;
    }

    public void addSelected(int index) {
        selectedPositions.add(index);
        notifyItemChanged(index);
    }

    public void addCurrentlySelected(int index) {
        currentlySelected.add(index);
        notifyItemChanged(index);
    }

    public void removeCurrentlySelected(int index) {
        currentlySelected.remove(index);
        notifyItemChanged(index);
    }

    public void setLetters(char[] letters) {
        this.letters = letters;
        notifyDataSetChanged();
    }

    public class WordSearchViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        ItemWordSearchBinding binding;

        public WordSearchViewHolder(@NonNull ItemWordSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnLongClickListener(this);
        }

        public void bind(char letter, int position) {
            String letterStr = String.valueOf(letter);
            // special case -- turkish uppercase i should have dot
            if (letterStr.equals("i") && targetLanguage.equals("tr")) {
                String capitalized = "\u0130";
                binding.tvLetter.setText(capitalized);
            } else {
                binding.tvLetter.setText(letterStr.toUpperCase());
            }
            if (selectedPositions.contains(position)) {
                Drawable highlight = fragment.getResources().getDrawable(R.drawable.word_search_highlight_correct);
                binding.getRoot().setBackground(highlight);
            } else if (currentlySelected.contains(position)) {
                Drawable highlight = fragment.getResources().getDrawable(R.drawable.word_search_highlight);
                binding.getRoot().setBackground(highlight);
            } else {
                binding.getRoot().setBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            dragSelectTouchListener.startDragSelection(getAdapterPosition());
            return true;
        }
    }
}
