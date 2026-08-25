package com.doctorview.app.models;

/**
 * One health news article.
 * Stored in the Cloud Firestore "news" collection.
 * The date is stored as "yyyy-MM-dd" so lists can sort it.
 */
public class NewsArticle {

    private String id;
    private String title;
    private String category;
    private String imageUrl;
    private String date;
    private String body;

    public NewsArticle() {
        // Required by Firestore
    }

    public NewsArticle(String id, String title, String category, String imageUrl,
                       String date, String body) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
        this.date = date;
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
