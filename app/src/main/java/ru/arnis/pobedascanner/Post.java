package ru.arnis.pobedascanner;

/**
 * Created by arnis on 19/08/16.
 */
public class Post {

    private int postID;
    private String text;
    private String imageURL;

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    private String timeStamp;

    public Post(String text, String imageURL,String timeStamp) {
        this.text = text;
        this.imageURL = imageURL;
        this.timeStamp = timeStamp;
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
