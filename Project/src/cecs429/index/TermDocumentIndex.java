package cecs429.index;

import java.util.*;

/**
 * Implements an Index using a term-document matrix. Requires knowing the full
 * corpus vocabulary and number of documents prior to construction.
 */
public class TermDocumentIndex implements Index {

    private final boolean[][] mMatrix;
    private final List<String> mVocabulary;
    private final int mCorpusSize;

    /**
     * Constructs an empty index with with given vocabulary set and corpus size.
     *
     * @param vocabulary a collection of all terms in the corpus vocabulary.
     * @param corpuseSize the number of documents in the corpus.
     */
    public TermDocumentIndex(Collection<String> vocabulary, int corpuseSize) {
        mMatrix = new boolean[vocabulary.size()][corpuseSize];
        mVocabulary = new ArrayList<>();
        mVocabulary.addAll(vocabulary);
        mCorpusSize = corpuseSize;

        Collections.sort(mVocabulary);
    }

    /**
     * Associates the given documentId with the given term in the index.
     *
     * @param term
     * @param documentId
     */
    public void addTerm(String term, int documentId) {
        int vIndex = Collections.binarySearch(mVocabulary, term);
        if (vIndex >= 0) {
            mMatrix[vIndex][documentId] = true;
        }
    }

    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> results = new ArrayList<>();
        // what if term is not in matrix
        int index = Collections.binarySearch(mVocabulary, term);
        try {

            for (int i = 0; i < mMatrix[index].length; i++) {
                if (mMatrix[index][i]) {
                    //TODO: Check if it is correct
                    results.add(new Posting(i));
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //TODO: Check if it is correct
            results.add(new Posting(-1));
        }
        return results;

    }

    @Override
    public List<String> getVocabulary() {
        return Collections.unmodifiableList(mVocabulary);
    }
}
