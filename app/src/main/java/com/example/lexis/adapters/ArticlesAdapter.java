package com.example.lexis.adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lexis.R;
import com.example.lexis.databinding.ItemArticleBinding;
import com.example.lexis.fragments.ArticleFragment;
import com.example.lexis.fragments.FeedFragment;
import com.example.lexis.models.Article;
import com.example.lexis.utilities.Utils;
import com.parse.ParseUser;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder> {
    List<Article> articles;
    FeedFragment fragment;

    public ArticlesAdapter(FeedFragment fragment, List<Article> articles) {
        this.fragment = fragment;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArticleBinding binding = ItemArticleBinding.inflate(LayoutInflater.from(fragment.getActivity()), parent, false);
        return new ArticleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void clear() {
        articles.clear();
        notifyDataSetChanged();
    }

    public void shuffle() {
        Collections.shuffle(articles);
        notifyDataSetChanged();
    }

    public void addAll(List<Article> list) {
        articles.addAll(list);
        notifyDataSetChanged();
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemArticleBinding binding;

        public ArticleViewHolder(@NonNull ItemArticleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
        }

        /*
        Bind Article data into the ViewHolder.
        */
        public void bind(Article article) {
            binding.tvTitle.setText(article.getTitle());
            binding.tvSnippet.setText(article.getBody());
            Integer logoId;
            switch (article.getSource()) {
                case "Wikipedia":
                    logoId = R.drawable.wikipedia_logo;
                    break;
                case "BBC News":
                    logoId = R.drawable.bbc_news_logo;
                    break;
                case "Wired":
                    logoId = R.drawable.wired_logo;
                    break;
                case "The Huffington Post":
                    logoId = R.drawable.huffington_post_logo;
                    break;
                case "Time":
                    logoId = R.drawable.time_logo;
                    break;
                case "Short stories":
                    logoId = R.drawable.aesop;
                    break;
                case "The New York Times":
                    logoId = R.drawable.new_york_times_logo;
                    break;
                default:
                    logoId = null;
            }

            if (logoId != null) {
                binding.ivSource.setImageResource(logoId);
            }
        }

        /*
        Translate article if necessary and show ArticleFragment when an item is clicked.
        */
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            fragment.showProgressBar();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                // only translate words if we haven't previously done so or if the user has changed their
                // target language since the article's translation
                Article article = articles.get(position);
                String currentTargetLanguage = Utils.getCurrentTargetLanguage();
                boolean isCorrectLanguage = article.getLanguage().equals(currentTargetLanguage);
                int translationInterval = Utils.getTranslationInterval(ParseUser.getCurrentUser());
                boolean isCorrectFrequency = (article.getFrequency() == translationInterval);

                if (article.getWordList() == null || !isCorrectLanguage || !isCorrectFrequency) {
                    article.translateWordsOnInterval(3, translationInterval);
                }

                // executed when async work is completed
                handler.post(() -> {
                    fragment.hideProgressBar();

                    AppCompatActivity activity = (AppCompatActivity) fragment.getActivity();
                    if (activity == null) return;

                    final FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    final Fragment articleFragment = ArticleFragment.newInstance(article);

                    fragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, articleFragment)
                            .addToBackStack(null)
                            .commit();
                });
            });
        }
    }
}
