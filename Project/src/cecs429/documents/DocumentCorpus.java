package cecs429.documents;

/**
 * Represents a collection of documents used to build an index.
 */
public interface DocumentCorpus {

    /**
     * Gets all documents in the corpus.
     *
     * @return returns an iterable over Document objects
     */
    Iterable<Document> getDocuments();

    /**
     * The number of documents in the corpus.
     *
     * @return return the size of the corpus indexed
     */
    int getCorpusSize();

    /**
     * Returns the document with the given document ID.
     *
     * @param id the id of the document to be retrieved
     * @return returns the Document that is associated with the id provided
     */
    Document getDocument(int id);
}
