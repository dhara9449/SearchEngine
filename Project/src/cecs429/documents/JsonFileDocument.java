/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.documents;

import cecs429.gson.JsonDocument;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;


public class JsonFileDocument implements FileDocument {

    private final int mDocumentId;
    private final Path mFilePath;
    private String mFileName;
    private String mFileTitle;

    /**
     * Constructs a JsonFileDocument with the given document ID representing the
     * file at the given absolute file path.
     *
     * @param id
     * @param absoluteFilePath
     */
    private JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
        setFileName();
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
            JsonDocument jsonDocument = gson.fromJson(jsonReader, JsonDocument.class);
            String content = jsonDocument.getBody();
            targetReader = new StringReader(content);
        }catch (FileNotFoundException e) {
            System.out.print(e);
        }
        return targetReader;
    }

    @Override
    public String getTitle() {
        Gson gson = new Gson();
        JsonDocument jsonDocument;
        try {
            JsonReader jsonReader = new JsonReader(new FileReader(mFilePath.toString()));
            jsonDocument = gson.fromJson(jsonReader, JsonDocument.class);
            mFileTitle = jsonDocument.getTitle();
        }
        catch (FileNotFoundException ignored) {
        }

        return mFileTitle;
    }

    private void setFileName() {
        mFileName = mFilePath.getFileName().toString();
    }

    public String getmFileName(){
        return mFileName;
    }

    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JsonFileDocument(documentId, absolutePath);
    }
}
