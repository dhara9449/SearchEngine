/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.documents;

import java.io.Reader;

public interface MovieDocument {

    /**
     * The ID used by the index to represent the document.
     * @return
     */
    int getId();

    Reader getContent();

    String getTitle();

    /**
     * Url from which the movie was scraped
     * */
    String getUrl();

    /**
     * The year the movie was released
     * */
    String getYear();

    /**
     * The genre the movie was listed under
     * */
    String getGenre();

    /**
     *  The movie's imdb rating
     *
     * */
    String getRating();
}
