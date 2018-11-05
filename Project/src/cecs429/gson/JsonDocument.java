/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author indumanimaran
 * @author DharaPatel
 */
public class JsonDocument {

    @SerializedName("body")
    private String body;
    @SerializedName("url")
    private String url;
    @SerializedName("title")
    private String title;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
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
}
