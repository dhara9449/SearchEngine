package cecs429.documents;

/**
 * Represents a collection of documents used to build an index.
 */
public interface DocumentCorpus {

    /**
     * Gets all documents in the corpus.
     *
     * @return
     */
    Iterable<Document> getDocuments();

    /**
     * The number of documents in the corpus.
     *
     * @return
     */
    int getCorpusSize();

    /**
     * Returns the document with the given document ID.
     *
     * @param id
     * @return
     */
    Document getDocument(int id);
}
