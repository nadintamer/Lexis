package com.example.lexis.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.R;
import com.example.lexis.databinding.ItemWordListBinding;
import com.example.lexis.models.Clue;

import java.util.List;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.WordListViewHolder> {

    List<Clue> clues;
    Fragment fragment;

    public WordListAdapter(Fragment fragment, List<Clue> clues) {
        this.fragment = fragment;
        this.clues = clues;
    }

    @NonNull
    @Override
    public WordListAdapter.WordListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordListBinding binding = ItemWordListBinding.inflate(LayoutInflater.from(fragment.getActivity()), parent, false);
        return new WordListAdapter.WordListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordListAdapter.WordListViewHolder holder, int position) {
        Clue clue = clues.get(position);
        holder.bind(clue);
    }

    @Override
    public int getItemCount() {
        return clues.size();
    }

    public void addAll(List<String> words) {
        for (String word : words) {
            clues.add(new Clue(word));
        }
        notifyDataSetChanged();
    }

    public class WordListViewHolder extends RecyclerView.ViewHolder {
        ItemWordListBinding binding;

        public WordListViewHolder(@NonNull ItemWordListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Clue clue) {
            binding.tvWord.setText(clue.getText());
            if (clue.isFound()) {
                binding.tvWord.setTextColor(fragment.getResources().getColor(R.color.light_gray));
                binding.tvWord.setPaintFlags(binding.tvWord.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                binding.tvWord.setTextColor(Color.BLACK);
            }
        }
    }
}
