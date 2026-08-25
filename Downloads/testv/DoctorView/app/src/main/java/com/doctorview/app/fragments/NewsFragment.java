package com.doctorview.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.adapters.NewsAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.NewsArticle;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.doctorview.app.utils.SampleData;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Healthcare News: a searchable feed of health articles from Firestore.
 * One tap seeds 8 sample articles for the demo.
 */
public class NewsFragment extends Fragment {

    private final List<NewsArticle> articles = new ArrayList<>();

    private RecyclerView rvNews;
    private TextInputEditText etSearch;
    private ProgressBar progressBar;
    private View emptyContainer;
    private TextView tvEmptyTitle;
    private TextView tvEmptyText;
    private Button btnLoadSamples;
    private Button btnRetry;
    private NewsAdapter adapter;

    public NewsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvNews = view.findViewById(R.id.rvNews);
        etSearch = view.findViewById(R.id.etSearch);
        progressBar = view.findViewById(R.id.progressBar);
        emptyContainer = view.findViewById(R.id.emptyContainer);
        tvEmptyTitle = view.findViewById(R.id.tvEmptyTitle);
        tvEmptyText = view.findViewById(R.id.tvEmptyText);
        btnLoadSamples = view.findViewById(R.id.btnLoadSamples);
        btnRetry = view.findViewById(R.id.btnRetry);

        rvNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NewsAdapter(articles, article -> {
            Bundle args = new Bundle();
            args.putString("articleId", article.getId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_newsFragment_to_newsDetailsFragment, args);
        });
        rvNews.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        btnLoadSamples.setOnClickListener(v -> seedSampleArticles());
        btnRetry.setOnClickListener(v -> loadArticles());

        loadArticles();
    }

    /** Loads all articles from Firestore, newest first. */
    private void loadArticles() {
        showLoading();

        FirebaseHelper.getFirestore()
                .collection(Constants.COLLECTION_NEWS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    articles.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        NewsArticle article = doc.toObject(NewsArticle.class);
                        article.setId(doc.getId());
                        articles.add(article);
                    }
                    sortNewestFirst();
                    adapter.setArticles(articles);
                    adapter.filter(etSearch.getText().toString());

                    if (articles.isEmpty()) {
                        showEmptyState(R.string.empty_news_title,
                                R.string.empty_news_text, true);
                    } else {
                        showList();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmptyState(R.string.error_load_news_title,
                                R.string.error_load_news_text, false);
                    }
                });
    }

    /** Dates are stored as yyyy-MM-dd, so comparing strings sorts correctly. */
    private void sortNewestFirst() {
        Collections.sort(articles, (a, b) -> {
            String dateA = a.getDate();
            String dateB = b.getDate();
            if (dateA == null && dateB == null) {
                return 0;
            }
            if (dateA == null) {
                return 1;
            }
            if (dateB == null) {
                return -1;
            }
            return dateB.compareTo(dateA);
        });
    }

    /** Writes the sample articles into Firestore (one tap, for the demo). */
    private void seedSampleArticles() {
        showLoading();

        WriteBatch batch = FirebaseHelper.getFirestore().batch();
        for (NewsArticle article : SampleData.getSampleArticles()) {
            batch.set(FirebaseHelper.getFirestore()
                    .collection(Constants.COLLECTION_NEWS)
                    .document(), article);
        }
        batch.commit()
                .addOnSuccessListener(a -> {
                    AppUtils.showToast(requireContext(), R.string.news_loaded);
                    loadArticles();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        showEmptyState(R.string.error_load_news_title,
                                R.string.error_load_news_text, false);
                    }
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvNews.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showList() {
        progressBar.setVisibility(View.GONE);
        rvNews.setVisibility(View.VISIBLE);
        emptyContainer.setVisibility(View.GONE);
    }

    private void showEmptyState(int titleRes, int textRes, boolean showSeedButton) {
        progressBar.setVisibility(View.GONE);
        rvNews.setVisibility(View.GONE);
        emptyContainer.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText(titleRes);
        tvEmptyText.setText(textRes);
        btnLoadSamples.setVisibility(showSeedButton ? View.VISIBLE : View.GONE);
        btnRetry.setVisibility(showSeedButton ? View.GONE : View.VISIBLE);
    }
}
