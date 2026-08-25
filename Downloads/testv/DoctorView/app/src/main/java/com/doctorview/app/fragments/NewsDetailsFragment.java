package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.doctorview.app.R;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.NewsArticle;
import com.doctorview.app.utils.Constants;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Full article view: banner image, category, date, title and body.
 */
public class NewsDetailsFragment extends Fragment {

    private View contentContainer;
    private ProgressBar progressBar;
    private TextView tvNotFound;

    public NewsDetailsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contentContainer = view.findViewById(R.id.contentContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvNotFound = view.findViewById(R.id.tvNotFound);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        String articleId = getArguments() != null ? getArguments().getString("articleId") : null;
        if (articleId == null || articleId.isEmpty()) {
            showNotFound();
        } else {
            loadArticle(articleId);
        }
    }

    /** Reads the article document from Firestore. */
    private void loadArticle(String articleId) {
        showLoading();

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_NEWS)
                .document(articleId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) {
                        return;
                    }
                    if (doc.exists()) {
                        NewsArticle article = doc.toObject(NewsArticle.class);
                        if (article != null) {
                            populateViews(article);
                            showContent();
                        } else {
                            showNotFound();
                        }
                    } else {
                        showNotFound();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showNotFound();
                    }
                });
    }

    /** Fills the screen with the article's data. */
    private void populateViews(NewsArticle article) {
        ShapeableImageView ivImage = contentContainer.findViewById(R.id.ivArticleImage);
        TextView tvCategory = contentContainer.findViewById(R.id.tvArticleCategory);
        TextView tvDate = contentContainer.findViewById(R.id.tvArticleDate);
        TextView tvTitle = contentContainer.findViewById(R.id.tvArticleTitle);
        TextView tvBody = contentContainer.findViewById(R.id.tvArticleBody);

        Glide.with(requireContext())
                .load(article.getImageUrl())
                .placeholder(R.drawable.ic_news)
                .error(R.drawable.ic_news)
                .into(ivImage);

        tvCategory.setText(article.getCategory());
        tvDate.setText(formatDate(article.getDate()));
        tvTitle.setText(article.getTitle());
        tvBody.setText(article.getBody());
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

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
        tvNotFound.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        tvNotFound.setVisibility(View.GONE);
    }

    private void showNotFound() {
        progressBar.setVisibility(View.GONE);
        contentContainer.setVisibility(View.GONE);
        tvNotFound.setVisibility(View.VISIBLE);
    }
}
