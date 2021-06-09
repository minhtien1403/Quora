package com.example.quora.models;

public class Post {
    private String askedby, date, postid, publisher, question, questionImage, topic;

    public Post() {
    }

    public Post(String askedby, String date, String postid, String publisher, String question, String questionImage, String topic) {
        this.askedby = askedby;
        this.date = date;
        this.postid = postid;
        this.publisher = publisher;
        this.question = question;
        this.questionImage = questionImage;
        this.topic = topic;
    }

    public String getAskedby() {
        return askedby;
    }

    public void setAskedby(String askedby) {
        this.askedby = askedby;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionImage() {
        return questionImage;
    }

    public void setQuestionImage(String questionImage) {
        this.questionImage = questionImage;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
