package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements QueryComponent {

    private List<QueryComponent> mComponents;

    public AndQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        List<Posting> result = new ArrayList<>() ;
        result.addAll(mComponents.get(0).getPostings(index));
        List<Posting> tempComponentPostingsList;
        for (int i = 1; i < mComponents.size(); i++) {
            tempComponentPostingsList = mComponents.get(i).getPostings(index);
            result = mergePosting(result, tempComponentPostingsList);
        }
        return result;
    }
    /*
     * merge for an AndQuery, by gathering the postings of the composed QueryComponents and
     *   intersecting the resulting postings.
     */
    public List<Posting> mergePosting(List<Posting> list1, List<Posting> list2) {
        List<Posting> mergeResult = new ArrayList<>();
        Posting p1, p2;
        int doc1PostingPtr = 0;
        int doc2PostingPtr = 0;
        while (doc1PostingPtr < list1.size() && doc2PostingPtr < list2.size()) {
            p1 = (Posting) list1.get(doc1PostingPtr);
            p2 = (Posting) list2.get(doc2PostingPtr);
            int p1Id = p1.getDocumentId();
            int p2Id = p2.getDocumentId();
            if (p1Id == p2Id) {
                mergeResult.add(p1);
                doc1PostingPtr++;
                doc2PostingPtr++;
            } else {
                if (p1Id < p2Id) {
                    doc1PostingPtr++;
                } else {
                    doc2PostingPtr++;
                }

            }
        }
        return mergeResult;
    }

    @Override
    public String toString() {
        return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
}
