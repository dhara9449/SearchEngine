package cecs429.index;

import cecs429.TermFrequency.ContextStrategy;

import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating
 * terms and the documents that contain them.
 */
public interface Index {

    /**
     * Retrieves a list of Postings of documents that contain the given term.
     *
     * @param term
     * @return
     */
    List<Posting> getPostings(String term);

    /**
     * A (sorted) list of all terms in the index vocabulary.
     *
     * @return
     */
    List<String> getVocabulary();

    int getVocabulorySize();

    List<Posting> getPostingsWithPosition(String term);
}
