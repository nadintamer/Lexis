package com.example.lexis.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.R;
import com.example.lexis.databinding.ItemWordListBinding;
import com.example.lexis.fragments.WordDialogFragment;
import com.example.lexis.models.Clue;
import com.example.lexis.models.Word;

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

    public void addAll(List<Word> words) {
        for (Word word : words) {
            clues.add(new Clue(word));
        }
        notifyDataSetChanged();
    }

    public class WordListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemWordListBinding binding;
        Clue clue;

        public WordListViewHolder(@NonNull ItemWordListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        public void bind(Clue clue) {
            this.clue = clue;

            binding.tvWord.setText(clue.getText());
            if (clue.isFound()) {
                binding.tvWord.setTextColor(fragment.getResources().getColor(R.color.light_gray));
                binding.tvWord.setPaintFlags(binding.tvWord.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.wordHighlight.setBackgroundResource(R.drawable.word_list_highlight_found);
            } else {
                binding.tvWord.setTextColor(Color.BLACK);
                binding.wordHighlight.setBackgroundResource(R.drawable.word_list_highlight);
            }
        }

        @Override
        public void onClick(View v) {
            TextView textView = v.findViewById(R.id.tvWord);

            // get XY-position of clicked word
            int[] position = new int[2];
            textView.getLocationInWindow(position);
            int x = position[0];
            int y = position[1];

            Rect wordPosition = new Rect(x, y, x + textView.getWidth(), y + textView.getHeight());
            launchWordDialog(clue.getSolution(), clue.getText(), wordPosition.left, wordPosition.top, wordPosition.width());
        }
    }

    /*
    Launch a word meaning dialog with the provided target language and English words, relative to
    the position of the word clicked (described by left, top, and width).
    */
    private void launchWordDialog(String targetLanguage, String english, int left, int top, int width) {
        AppCompatActivity activity = (AppCompatActivity) fragment.getActivity();
        if (activity != null) {
            FragmentManager fm = activity.getSupportFragmentManager();
            WordDialogFragment wordDialogFragment = WordDialogFragment.newInstance(
                    targetLanguage, english, left, top, width);
            wordDialogFragment.show(fm, "fragment_dialog");
        }
    }
}
