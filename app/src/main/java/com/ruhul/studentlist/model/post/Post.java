package com.ruhul.studentlist.model.post;
public class Post {

    private int userId;
    private String title;

    public Post(int userId, String title) {
        this.userId = userId;
        this.title = title;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

}
