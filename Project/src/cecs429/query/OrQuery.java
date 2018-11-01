package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a
 * union-type operation.
 */
public class OrQuery implements QueryComponent {
    // The components of the Or query.

    private final List<QueryComponent> mComponents;

    public OrQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    @Override
    public String toString() {
        // Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
        return "("
                + String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
                + " )";
    }

    @Override
    public List<Posting> getPostings(Index index,String mode) {

        List<Posting> result = mComponents.get(0).getPostings(index,mode);
        List<Posting> tempComponentPostingsList;

        for (int postingListNum = 1; postingListNum < mComponents.size(); postingListNum++) {
            tempComponentPostingsList = mComponents.get(postingListNum).getPostings(index,mode);

            if (!result.isEmpty()) {
                result = mergePosting(result, tempComponentPostingsList);
            } else {
                if (!tempComponentPostingsList.isEmpty()) {
                    result = tempComponentPostingsList;
                }
            }
        }
        return result;
    }

    /* the merge for an OrQuery, by gathering the postings of the
     composed QueryComponents and
      */
    public List<Posting> mergePosting(List<Posting> list1, List<Posting> list2) {
        List<Posting> mergeResult = new ArrayList<>();
        Posting p1, p2;
        int postingDoc1Ptr = 0;
        int postingDoc2Ptr = 0;
        while (postingDoc1Ptr < list1.size() && postingDoc2Ptr < list2.size()) {
            p1 = (Posting) list1.get(postingDoc1Ptr);
            p2 = (Posting) list2.get(postingDoc2Ptr);
            int p1Id = p1.getDocumentId();
            int p2Id = p2.getDocumentId();

            if (p1Id < p2Id) {
                mergeResult.add(p1);
                postingDoc1Ptr++;
            } else if (p1Id > p2Id) {
                mergeResult.add(p2);
                postingDoc2Ptr++;
            } else {
                mergeResult.add(p1);
                postingDoc1Ptr++;
                postingDoc2Ptr++;
            }
        }
        if (postingDoc1Ptr <= list1.size()) {
            for (int k = postingDoc1Ptr; k < list1.size(); k++) {
                mergeResult.add((Posting) list1.get(k));
            }
        }
        if (postingDoc2Ptr <= list2.size()) {
            for (int k = postingDoc2Ptr; k < list2.size(); k++) {
                mergeResult.add((Posting) list2.get(k));
            }
        }
        return mergeResult;
    }
}
