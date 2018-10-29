package cecs429.documents;

import java.io.Reader;

/**
 * Represents a document in an index.
 */
public interface Document {

    /**
     * The ID used by the index to represent the document.
     *
     * @return
     */
    int getId();

    /**
     * Gets a stream over the content of the document.
     *
     * @return
     */
    Reader getContent();

    /**
     * The title of the document, for displaying to the user.
     *
     * @return
     */
    String getTitle();

    /**
     * Returns the name of the document
     * Note: This is different from title
     * **/
    String getmFileName();

}
