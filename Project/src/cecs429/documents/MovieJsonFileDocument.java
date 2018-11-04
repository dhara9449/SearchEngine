/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.documents;

import cecs429.gson.MovieJsonDocument;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

public class MovieJsonFileDocument implements FileDocument {

    private final int mDocumentId;
    private final Path mFilePath;
    private String mFileName;

    /**
     * Constructs a JsonFileDocument with the given document ID representing the
     * file at the given absolute file path.
     *
     * @param id
     * @param absoluteFilePath
     */
    private MovieJsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
        setFileName();
    }

    public String getmFileName(){
        return mFileName;
    }
    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Reader getContent() {
        Reader targetReader = null;
        JsonReader jsonReader;
        Gson gson = new Gson();
        try {
            jsonReader = new JsonReader(new FileReader(mFilePath.toString()));
            MovieJsonDocument jsonDocument = gson.fromJson(jsonReader, MovieJsonDocument.class);
            String content = jsonDocument.getScript();
            // System.out.println("Printing content @JsonFileDocument: "+content);
            targetReader = new StringReader(content);
        } catch (FileNotFoundException e) {
            System.out.print(e);
        }

        return targetReader;
    }

    @Override
    public String getTitle() {
        return mFileName;
    }

    private void setFileName() {
        Gson gson = new Gson();
        MovieJsonDocument jsonDocument;
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(mFilePath.toString()));
            jsonDocument = gson.fromJson(jsonReader, MovieJsonDocument.class);
            mFileName = jsonDocument.getTitle();
        } catch (FileNotFoundException ignored) {
        }
    }

    static FileDocument loadMovieJsonFileDocument(Path absolutePath, int documentId) {
        return new MovieJsonFileDocument(documentId, absolutePath);
    }
}
