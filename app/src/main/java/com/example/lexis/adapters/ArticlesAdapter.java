package com.example.lexis.adapters;

import android.os.AsyncTask;
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

import java.util.List;

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
            if (article.getSource().equals("Wikipedia")) {
                binding.ivSource.setImageResource(R.drawable.wikipedia_logo);
            }
        }

        /*
        Translate article if necessary and show ArticleFragment when an item is clicked.
        */
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            TranslateTask task = new TranslateTask();
            task.execute(position);
        }

        // TODO: AsyncTask is deprecated so I am unsure if I should be using something else instead
        class TranslateTask extends AsyncTask<Integer, Void, Integer> {
            @Override
            protected Integer doInBackground(Integer... integers) {
                int position = integers[0];

                // only translate words if we haven't previously done so or if the user has changed their
                // target language since the article's translation
                Article article = articles.get(position);
                String currentTargetLanguage = Utils.getCurrentTargetLanguage();
                boolean isCorrectLanguage = article.getLanguage().equals(currentTargetLanguage);
                if (article.getWordList() == null || !isCorrectLanguage) {
                    article.translateWordsOnInterval(3, 60);
                }

                return position;
            }

            @Override
            protected void onPostExecute(Integer position) {
                fragment.hideProgressBar();

                AppCompatActivity activity = (AppCompatActivity) fragment.getActivity();
                if (activity == null) return;

                final FragmentManager fragmentManager = activity.getSupportFragmentManager();
                final Fragment articleFragment = ArticleFragment.newInstance(articles.get(position));

                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, articleFragment)
                        .addToBackStack(null) // add to back stack so we can return to this fragment
                        .commit();
            }

            @Override
            protected void onPreExecute() {
                fragment.showProgressBar();
            }
        }
    }
}
