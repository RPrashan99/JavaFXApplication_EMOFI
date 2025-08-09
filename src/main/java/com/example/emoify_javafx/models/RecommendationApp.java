package com.example.emoify_javafx.models;

public class RecommendationApp {

    private String app_name;
    private String app_url;
    private String search_query;
    private Boolean isLocal;

    public RecommendationApp(String app_name, String app_url, String search_query, Boolean isLocal) {
        this.app_name = app_name;
        this.app_url = app_url;
        this.search_query = search_query;
        this.isLocal = isLocal;
    }

    public String getApp_name() {
        return app_name;
    }

    public String getApp_url() {
        return app_url;
    }

    public String getSearch_query() {
        return search_query;
    }

    public Boolean getLocal() {
        return isLocal;
    }
}
