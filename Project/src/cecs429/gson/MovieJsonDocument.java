/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.gson;

import com.google.gson.annotations.SerializedName;


public class MovieJsonDocument {

    /**
     * Title : Title of the movie
     */
    @SerializedName("title")
    public String title;

    /**
     * Script : screenplay of the movie
     */
    @SerializedName("script")
    public String script;

    /**
     * url : url from which the screenplay was scraped
     */
    @SerializedName("url")
    public String url;

    /**
     * year: movie released
     */
    @SerializedName("year")
    public String year;

    /**
     * genre: of the movie
     */
    @SerializedName("genre")
    public String genre;

    /**
     * rating: imdb rating
     */
    @SerializedName("rating")
    public String rating;

    public String getScript() {
        return script;
    }

    public void setScript(String body) {
        this.script = body;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getGenre() {
        return genre;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
