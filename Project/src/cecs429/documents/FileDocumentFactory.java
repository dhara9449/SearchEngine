package cecs429.documents;

import java.nio.file.Path;

/**
 * as the name indicates , a factory method that pushes out filedocuments
 * */
public interface FileDocumentFactory {

    FileDocument createFileDocument(Path absoluteFilePath, int documentId);
}
