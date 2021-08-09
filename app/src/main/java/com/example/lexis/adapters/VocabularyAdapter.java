package com.example.lexis.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.databinding.ItemVocabularyBinding;
import com.example.lexis.fragments.PracticeFragment;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Utils;
import com.google.android.material.snackbar.Snackbar;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    List<Word> vocabulary;
    PracticeFragment fragment;

    public VocabularyAdapter(PracticeFragment fragment, List<Word> vocabulary) {
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

    public void insertAt(int position, Word word) {
        vocabulary.add(position, word);
        notifyItemInserted(position);
        fragment.checkVocabularyEmpty(vocabulary);
    }

    public void clear() {
        vocabulary.clear();
        notifyDataSetChanged();
    }

    /*
    Delete word at given position from user's vocabulary.
    */
    public void deleteWord(int position) {
        Word word = vocabulary.get(position);
        String targetWord = word.getTargetWord();
        word.deleteInBackground();
        vocabulary.remove(position);
        notifyItemRemoved(position);
        fragment.checkVocabularyEmpty(vocabulary);

        Snackbar.make(fragment.binding.btnPractice, "Deleted word: " + targetWord, Snackbar.LENGTH_LONG)
                .setAction("Undo", view -> {
                    Word newWord = Word.copyWord(word);
                    newWord.saveInBackground();

                    vocabulary.add(position, word);
                    notifyItemInserted(position);
                    fragment.binding.rvVocabulary.scrollToPosition(position);
                }).show();
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
            binding.tvTargetLanguage.setText(word.getTargetWord());
            binding.tvEnglish.setText(word.getEnglishWord());
            binding.tvFlag.setText(Utils.getFlagEmoji(word.getTargetLanguage()));

            binding.btnStar.setLiked(word.getIsStarred());
            binding.btnStar.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    toggleStarred(word);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    toggleStarred(word);
                }
            });
        }

        /*
        Toggle whether the word is starred and save to Parse.
        */
        private void toggleStarred(Word word) {
            word.toggleIsStarred();
            word.saveInBackground();
        }
    }
}
