package com.gaiusnetworks.gaius;

public class Channel {
    private int id;
    private String title;
    private String link;
    private String image;

    public Channel(int id, String title, String link, String image) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.image = "http://91.230.41.34:8080/test/"+image;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getImage() {
        return image;
    }
}
