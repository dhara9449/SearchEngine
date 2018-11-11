/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.gson;

import com.google.gson.annotations.SerializedName;


public class MlbJsonDocument {

    /**
     * url : url from which the screenplay was scraped
     */
    @SerializedName("url")
    public String url;


    /**
     * Title : Title of the movie
     */
    @SerializedName("title")
    public String title;

    /**
     * year: movie released
     */
    @SerializedName("subtitle")
    public String subtitle;

    /**
     * Script : screenplay of the movie
     */
    @SerializedName("body")
    public String body;


    /**
     * rating: imdb rating
     */
    @SerializedName("author")
    public String author;

    public String getBody() {
        return body;
    }

    public void setScript(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }


}
