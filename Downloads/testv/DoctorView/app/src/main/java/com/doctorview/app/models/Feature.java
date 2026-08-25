package com.doctorview.app.models;

/**
 * One quick-access card on the Home screen.
 * Holds the text, icon, accent color and the navigation action
 * that opens the matching section.
 */
public class Feature {

    private final String title;
    private final int iconRes;
    private final int colorRes;
    private final int navActionRes;

    public Feature(String title, int iconRes, int colorRes, int navActionRes) {
        this.title = title;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.navActionRes = navActionRes;
    }

    public String getTitle() {
        return title;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getColorRes() {
        return colorRes;
    }

    public int getNavActionRes() {
        return navActionRes;
    }
}
