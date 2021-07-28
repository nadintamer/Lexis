package com.example.lexis.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.databinding.ItemWordSearchBinding;

public class WordSearchAdapter extends RecyclerView.Adapter<WordSearchAdapter.WordSearchViewHolder> {

    char[] letters;
    Fragment fragment;

    public WordSearchAdapter(Fragment fragment, char[] letters) {
        this.fragment = fragment;
        this.letters = letters;
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
        holder.bind(letter);
    }

    @Override
    public int getItemCount() {
        return letters.length;
    }

    public void setLetters(char[] letters) {
        this.letters = letters;
        notifyDataSetChanged();
    }

    public class WordSearchViewHolder extends RecyclerView.ViewHolder {
        ItemWordSearchBinding binding;

        public WordSearchViewHolder(@NonNull ItemWordSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(char letter) {
            binding.tvLetter.setText(String.valueOf(letter).toUpperCase());
        }
    }
}
