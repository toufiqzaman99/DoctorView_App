package com.doctorview.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.doctorview.app.R;
import com.doctorview.app.models.NewsArticle;
import com.doctorview.app.utils.AppUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Shows health news articles as cards with a banner image,
 * category chip, title and date. Searchable by title/category.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    /** Called when the user taps an article card. */
    public interface OnArticleClickListener {
        void onArticleClick(NewsArticle article);
    }

    private final List<NewsArticle> allArticles = new ArrayList<>();
    private final List<NewsArticle> filteredArticles = new ArrayList<>();
    private final OnArticleClickListener listener;

    public NewsAdapter(List<NewsArticle> articles, OnArticleClickListener listener) {
        this.listener = listener;
        setArticles(articles);
    }

    /** Replaces the whole list (e.g. after loading from Firestore). */
    public void setArticles(List<NewsArticle> articles) {
        allArticles.clear();
        allArticles.addAll(articles);
        filteredArticles.clear();
        filteredArticles.addAll(articles);
        notifyDataSetChanged();
    }

    /** Keeps only articles whose title or category contains the query. */
    public void filter(String query) {
        String q = query.trim().toLowerCase(Locale.getDefault());
        filteredArticles.clear();
        if (q.isEmpty()) {
            filteredArticles.addAll(allArticles);
        } else {
            for (NewsArticle article : allArticles) {
                if (contains(article.getTitle(), q) || contains(article.getCategory(), q)) {
                    filteredArticles.add(article);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(query);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsArticle article = filteredArticles.get(position);

        holder.tvTitle.setText(article.getTitle());
        holder.tvCategory.setText(article.getCategory());
        holder.tvDate.setText(formatDate(article.getDate()));
        holder.tvDesc.setText(AppUtils.truncate(article.getBody(), 90));

        Glide.with(holder.itemView.getContext())
                .load(article.getImageUrl())
                .placeholder(R.drawable.ic_news)
                .error(R.drawable.ic_news)
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> listener.onArticleClick(article));
    }

    @Override
    public int getItemCount() {
        return filteredArticles.size();
    }

    /** "yyyy-MM-dd" → "15 Aug 2026" */
    private String formatDate(String dateIso) {
        if (dateIso == null) {
            return "";
        }
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateIso);
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
        } catch (ParseException ignored) {
            return dateIso;
        }
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivImage;
        final TextView tvCategory;
        final TextView tvDate;
        final TextView tvTitle;
        final TextView tvDesc;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivNewsImage);
            tvCategory = itemView.findViewById(R.id.tvNewsCategory);
            tvDate = itemView.findViewById(R.id.tvNewsDate);
            tvTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvDesc = itemView.findViewById(R.id.tvNewsDesc);
        }
    }
}
