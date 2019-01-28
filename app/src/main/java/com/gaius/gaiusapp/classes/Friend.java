package com.gaius.gaiusapp.classes;

public class Friend {
    private int id;
    private String name;
    private String phoneNumber;
    private String image;
    private String userID;
    private String buttonType;

    public Friend(int id, String name, String phoneNumber, String image, String userID, String buttonType) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.image = "http://91.230.41.34:8080/test/"+image;
        this.userID = userID;
        this.buttonType = buttonType;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public String getUserID() { return userID; }

    public String getButtonType() {
        return buttonType;
    }
}
