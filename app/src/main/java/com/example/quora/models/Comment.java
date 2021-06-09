package com.example.quora.models;

public class Comment {
    private String comment, commentid, date, postid, publisher;

    public Comment() {}

    public Comment(String comment, String commentid, String date, String postid, String publisher) {
        this.comment = comment;
        this.commentid = commentid;
        this.date = date;
        this.postid = postid;
        this.publisher = publisher;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
