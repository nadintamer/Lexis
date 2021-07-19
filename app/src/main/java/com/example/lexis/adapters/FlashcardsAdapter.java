package com.example.lexis.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.databinding.ItemFlashcardBinding;
import com.example.lexis.models.Word;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlashcardsAdapter extends RecyclerView.Adapter<FlashcardsAdapter.FlashcardViewHolder> {
    List<Word> words;
    Fragment fragment;

    public FlashcardsAdapter(Fragment fragment, List<Word> words) {
        this.fragment = fragment;
        this.words = words;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ItemFlashcardBinding binding = ItemFlashcardBinding.inflate(LayoutInflater.from(fragment.getActivity()), parent, false);
        return new FlashcardsAdapter.FlashcardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FlashcardViewHolder holder, int position) {
        Word word = words.get(position);
        holder.bind(word);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void addAll(List<Word> list) {
        words.addAll(list);
        notifyDataSetChanged();
    }

    public class FlashcardViewHolder extends RecyclerView.ViewHolder {
        ItemFlashcardBinding binding;

        public FlashcardViewHolder(@NonNull ItemFlashcardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /*
        Bind Word data into the ViewHolder.
        */
        public void bind(Word word) {
            binding.layoutFront.tvWord.setText(word.getEnglishWord());
            binding.layoutRear.tvWord.setText(word.getTargetWord());
        }
    }
}
