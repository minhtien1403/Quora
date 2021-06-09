package com.example.quora.models;

public class User {
    private String email, fullname, id, profileImageUrl, username;

    public User() {

    }

    public User(String email, String fullname, String id, String profileImageUrl, String username) {
        this.email = email;
        this.fullname = fullname;
        this.id = id;
        this.profileImageUrl = profileImageUrl;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProfileimageurl() {
        return profileImageUrl;
    }

    public void setProfileimageurl(String profileimageurl) {
        this.profileImageUrl = profileimageurl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
