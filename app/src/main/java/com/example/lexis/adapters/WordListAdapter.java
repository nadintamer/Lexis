package com.example.lexis.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.databinding.ItemWordListBinding;

import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordListViewHolder> {

    List<String> words;
    Fragment fragment;

    public WordListAdapter(Fragment fragment, List<String> words) {
        this.fragment = fragment;
        this.words = words;
    }

    @NonNull
    @Override
    public WordListAdapter.WordListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordListBinding binding = ItemWordListBinding.inflate(LayoutInflater.from(fragment.getActivity()), parent, false);
        return new WordListAdapter.WordListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordListAdapter.WordListViewHolder holder, int position) {
        String word = words.get(position);
        holder.bind(word);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void addAll(List<String> clues) {
        words.addAll(clues);
        notifyDataSetChanged();
    }

    public class WordListViewHolder extends RecyclerView.ViewHolder {
        ItemWordListBinding binding;

        public WordListViewHolder(@NonNull ItemWordListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String word) {
            binding.tvWord.setText(word);
        }
    }
}
