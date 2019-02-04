package com.gaius.gaiusapp.classes;

public class Web {
    private int id;
    private String title;
    private String url;
    private String userID;
    private String image;

    public Web(int id, String title, String url, String userID, String image) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.userID = userID;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getUserID() {
        return userID;
    }

    public String getImage() {
        return image;
    }
}
