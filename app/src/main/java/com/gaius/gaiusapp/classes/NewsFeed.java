package com.gaius.gaiusapp.classes;

import java.util.ArrayList;

public class NewsFeed {
    private int id;
    private String name;
    private String updateTime;
    private String avatar;
    private String image;
    private String title;
    private String description;
    private String url;
    private String type;
    private Boolean showAvatar;
    private ArrayList<String> imagesGallery;
    private String liked;

    public NewsFeed (int id, String name, String updateTime, String avatar, String image, String title, String description, String url, String type, String liked, Boolean showAvatar, ArrayList<String> imagesGallery) {
        this.id = id;
        this.name = name;
        this.updateTime = updateTime;
        this.avatar = "http://91.230.41.34:8080/test/"+avatar;
        this.image = "http://91.230.41.34:8080/test/"+image;
        this.title = title;
        this.description = description;
        this.url = url;
        this.type = type;
        this.showAvatar = showAvatar;
        this.liked = liked;
        this.imagesGallery = imagesGallery;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {return url; }

    public String getType() {return type; }

    public Boolean getShowAvatar() {
        return showAvatar;
    }

    public String getLiked() {
        return liked;
    }

    public ArrayList<String> getImagesGallery() {
        return imagesGallery;
    }
}
