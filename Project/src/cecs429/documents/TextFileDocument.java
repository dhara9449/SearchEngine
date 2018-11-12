package cecs429.documents;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a document that is saved as a simple text file in the local file
 * system.
 */
public class TextFileDocument implements FileDocument {

    private final int mDocumentId;
    private final Path mFilePath;

    /**
     * Constructs a TextFileDocument with the given document ID representing the
     * file at the given absolute file path.
     *
     * @param id
     * @param absoluteFilePath
     */
    public TextFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;

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
        try {
            return Files.newBufferedReader(mFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getmFileName(){
        return mFilePath.getFileName().toString();
    }
    @Override
    public String getTitle() {
        return mFilePath.getFileName().toString();
    }

    public static FileDocument loadTextFileDocument(Path absolutePath, int documentId) {
        return new TextFileDocument(documentId, absolutePath);
    }

    @Override
    public double getByteSize(){
        return new File(mFilePath.toString()).length();
    }

}
