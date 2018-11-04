package cecs429.index;

import java.util.*;

/**
 * Implements an Index using a Map of term and list of postings.
 */
public class PositionalInvertedIndex implements Index {

    private Map<String, List<Posting>> mInvertedIndexMap;

    public PositionalInvertedIndex() {
        this.mInvertedIndexMap = new HashMap<>();
    }

    @Override
    public List<Posting> getPostings(String term) {
        // this is where perform ranked vs boolean retrieval
        List<Posting> temp = new ArrayList<>();
        if (mInvertedIndexMap.containsKey(term)) {
            return mInvertedIndexMap.get(term);
        }
        return temp;
    }

    @Override
    public List<String> getVocabulary() {
        List<String> mVocabulary = new ArrayList<>(mInvertedIndexMap.keySet());
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);
    }


    // TODO: implement this!!
    @Override
    public List<Integer> getPostingsWithoutPosition(String term) {
        return null;
    }

    /*
    adds the term in the existing hashmap
     */
    public void addTerm(String term, int documentId, int position) {

        //check if the term already  exists in the inverted index hashmap
        if (mInvertedIndexMap.containsKey(term)) {
            List<Posting> postings = mInvertedIndexMap.get(term);
            int postingLength = postings.size() - 1;

            //if the term occurs for the first time in the document

            if (postings.get(postingLength).getDocumentId()==documentId) {
                Posting p=postings.get(postingLength);
                p.getPositions().add(position);
            }
            //else the term exists in the current document

            else {
                Posting recentPosting = new Posting(documentId);
                recentPosting.addPosition(position);
                postings.add(recentPosting);
            }
        }
        //otherwise create a new entry for the term in inverted index hashmap
        else {
            List<Posting> tempPostingList = new ArrayList<>();
            Posting posting = new Posting(documentId);
            posting.addPosition(position);
            tempPostingList.add(posting);
            mInvertedIndexMap.put(term, tempPostingList);
        }
    }
}
