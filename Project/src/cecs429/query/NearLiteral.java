package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.BetterTokenProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NearLiteral implements QueryComponent {

    private List<String> mTerms;
    private int k = 1; // k=1 is default
    BetterTokenProcessor processor = new BetterTokenProcessor();

    /**
     * Constructs a NearLiteral with the given individual phrase terms.
     *
     * @param terms
     */
    public NearLiteral(List<String> terms) {
        if (!mTerms.contains("-")) {
            terms.forEach((str) -> {
                mTerms.addAll(processor.processToken(str));
            });
        } else {
            mTerms.addAll(terms);
        }
    }

    /**
     * Constructs a NearLiteral given a string with one or more individual
     * terms separated by spaces.
     *
     * @param terms
     */
    public NearLiteral(String terms) {
        mTerms = new ArrayList<>();
        if (!terms.contains("-")) {
            for (String str : Arrays.asList(terms.split(" "))) {
                mTerms.addAll(processor.processToken(str));
            }
        } else {
            mTerms = Arrays.asList(terms.split(" "));
        }
    }

    @Override
    public List<Posting> getPostings(Index index) {

        // Retrieving the postings for the individual terms in the phrase,
        // and positional merge them together.
        List<Posting> result = new ArrayList<>();
        String near = mTerms.get(1); // String containing NEAR
        try{
            k = Integer.parseInt(near.substring(5));
            if(k<1){
                return result;
            }
        } catch(Exception e){
            System.out.print("Invalid k value");
            return result;
        }

        List<Posting> tempComponentPostingsList1 = index.getPostings(mTerms.get(0));
        List<Posting> tempComponentPostingsList2 = index.getPostings(mTerms.get(2));

        return mergePosting(tempComponentPostingsList1, tempComponentPostingsList2);

    }

    private List<Posting> mergePosting(List<Posting> list1, List<Posting> list2) {
        List<Posting> mergeResult = new ArrayList<>();
        Posting posting1, posting2;
        int doc1PostingPtr1 = 0;
        int doc2PostingPtr2 = 0;

        while (doc1PostingPtr1 < list1.size() && doc2PostingPtr2 < list2.size()) {
            posting1 = (Posting) list1.get(doc1PostingPtr1);
            posting2 = (Posting) list2.get(doc2PostingPtr2);
            int p1Id = posting1.getDocumentId();
            int p2Id = posting2.getDocumentId();
            if (p1Id == p2Id) {
                List<Integer> indexPositionsDoc1 = posting1.getPositions();
                List<Integer> indexPositionsDoc2 = posting2.getPositions();
                doc1PostingPtr1++;
                doc2PostingPtr2++;
                List<Integer> tmp = mergePositions(indexPositionsDoc1, indexPositionsDoc2, k);
                if (tmp.size() > 0) {
                    mergeResult.add(new Posting(p1Id));
                }
            } else if (p1Id < p2Id) {
                doc1PostingPtr1++;
            } else {
                doc2PostingPtr2++;
            }
        }
        return mergeResult;
    }

    /**
     * K- positive integer that indicates the at most acceptable Hamming distance
     * a position from list2 can be from list1
     * */
    private List<Integer> mergePositions(List<Integer> posList1, List<Integer> posList2, int k) {

        List<Integer> mergePosResult = new ArrayList<>();

        while (k > 0) {
            int posList1Ptr = 0, posList2Ptr = 0;
            while (posList1Ptr < posList1.size() && posList2Ptr < posList2.size()) {
                int pos1 = posList1.get(posList1Ptr);
                int pos2 = posList2.get(posList2Ptr);

                if (pos1 + k == pos2) {
                    mergePosResult.add(pos1);
                    mergePosResult.add(pos2);
                    posList1Ptr++;
                    posList2Ptr++;

                } else if (pos1 + k < pos2) {
                    posList1Ptr++;
                } else {
                    posList2Ptr++;
                }
            }
            k--;
        }
        return mergePosResult;
    }

    @Override
    public String toString() {
        return "\"" + String.join(" ", mTerms) + "\"";
    }
}
