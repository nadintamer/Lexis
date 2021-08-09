package com.example.lexis.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.databinding.ItemFlashcardBinding;
import com.example.lexis.models.Word;
import com.example.lexis.utilities.Utils;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FlashcardsAdapter extends RecyclerView.Adapter<FlashcardsAdapter.FlashcardViewHolder> {
    List<Word> words;
    Fragment fragment;
    boolean answerInEnglish;

    public FlashcardsAdapter(Fragment fragment, List<Word> words, boolean answerInEnglish) {
        this.fragment = fragment;
        this.words = words;
        this.answerInEnglish = answerInEnglish;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ItemFlashcardBinding binding = ItemFlashcardBinding.inflate(
                LayoutInflater.from(fragment.getActivity()), parent, false);
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

    public void add(Word word) {
        words.add(word);
        notifyItemInserted(words.size() - 1);
    }

    public void remove(int index) {
        words.remove(index);
        notifyItemRemoved(index);
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
            // flip card to ensure front side is always shown first
            binding.flipView.flipSilently(false);

            LikeButton starButtonFront = binding.layoutFront.btnStar;
            LikeButton starButtonRear = binding.layoutRear.btnStar;

            starButtonFront.setLiked(word.getIsStarred());
            starButtonRear.setLiked(word.getIsStarred());
            starButtonFront.setOnLikeListener(createOnLikeListener(word));
            starButtonRear.setOnLikeListener(createOnLikeListener(word));

            String english = word.getEnglishWord();
            String target = word.getTargetWord();
            String englishFlag = Utils.getFlagEmoji("en");
            String targetFlag = Utils.getFlagEmoji(word.getTargetLanguage());

            if (answerInEnglish) {
                binding.layoutFront.tvWord.setText(target);
                binding.layoutFront.tvFlag.setText(targetFlag);

                binding.layoutRear.tvWord.setText(english);
                binding.layoutRear.tvFlag.setText(englishFlag);
            } else {
                binding.layoutFront.tvWord.setText(english);
                binding.layoutFront.tvFlag.setText(englishFlag);

                binding.layoutRear.tvWord.setText(target);
                binding.layoutRear.tvFlag.setText(targetFlag);
            }
        }

        /*
        Toggle whether the word is starred and save to Parse.
        */
        private void toggleStarred(Word word) {
            word.toggleIsStarred();
            word.saveInBackground();
        }

        /*
        Create an onLikeListener to handle starring/un-starring words.
        */
        private OnLikeListener createOnLikeListener(Word word) {
            return new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    binding.layoutFront.btnStar.setLiked(true);
                    binding.layoutRear.btnStar.setLiked(true);
                    toggleStarred(word);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    binding.layoutFront.btnStar.setLiked(false);
                    binding.layoutRear.btnStar.setLiked(false);
                    toggleStarred(word);
                }
            };
        }
    }
}
