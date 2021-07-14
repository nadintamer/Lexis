package com.example.lexis.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.databinding.ItemVocabularyBinding;
import com.example.lexis.models.Word;

import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    List<Word> vocabulary;
    Fragment fragment;

    public VocabularyAdapter(Fragment fragment, List<Word> vocabulary) {
        this.fragment = fragment;
        this.vocabulary = vocabulary;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabularyBinding binding = ItemVocabularyBinding.inflate(LayoutInflater.from(fragment.getActivity()), parent, false);
        return new VocabularyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        Word word = vocabulary.get(position);
        holder.bind(word);
    }

    @Override
    public int getItemCount() {
        return vocabulary.size();
    }

    public void addAll(List<Word> words) {
        vocabulary.addAll(words);
        notifyDataSetChanged();
    }

    public class VocabularyViewHolder extends RecyclerView.ViewHolder {
        ItemVocabularyBinding binding;

        public VocabularyViewHolder(@NonNull ItemVocabularyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /*
        Bind Word data into the ViewHolder.
        */
        public void bind(Word word) {
            binding.tvTargetLanguage.setText(word.getTargetLanguage());
            binding.tvEnglish.setText(word.getEnglish());
        }
    }
}
