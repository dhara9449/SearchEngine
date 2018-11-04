package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class PhraseLiteral implements QueryComponent {
    // The list of individual terms in the phrase.

    private List<String> mTerms;

    /**
     * Constructs a PhraseLiteral with the given individual phrase terms.
     *
     * @param terms
     */
    public PhraseLiteral(List<String> terms,TokenProcessor processor) {

        for( int j=0;j<mTerms.size();j++){
            String str = mTerms.get(j);
            mTerms.add(processor.processToken(str).get(0));
        }
    }

    /**
     * Constructs a PhraseLiteral given a string with one or more individual
     * terms separated by spaces.
     *
     * @param terms
     */
    PhraseLiteral(String terms, TokenProcessor processor) {
        mTerms = new ArrayList<>();
        for (String str : terms.split(" ")) {
            mTerms.add(processor.processToken(str).get(0));
        }
    }

    @Override
    public List<Posting> getPostings(Index index,String mode) {

     /*
      * Uncomment the following code for calling biword index on Phase literal queries with two terms.
      * Unless the below is uncommented, The current version of the program will call PhaseLiteral by default.
      *
      * /

    /*
    //using biword index for phrases with length 2

     if (mTerms.size() == 2) {
            return BiwordIndex.getIndex().getPostings(mTerms.get(0) + " " + mTerms.get(1));
        }
    */
        List<Posting> result = index.getPostings(mTerms.get(0),mode);
        List<Posting> tempComponentPostingsList;

        for (int i = 1; i < mTerms.size(); i++) {
            tempComponentPostingsList = index.getPostings(mTerms.get(i),mode);
            result = mergePosting(result, tempComponentPostingsList);

        }

        List<Integer> tmpPositionList;
        int decLength = mTerms.size();
        List<Posting> finalResult=new ArrayList<>();
        for (Posting p : result) {
            tmpPositionList = p.getPositions();
            Posting q=new Posting(p.getDocumentId());
            for (Integer aTmpPositionList : tmpPositionList) {
                q.addPosition(aTmpPositionList - decLength);
            }
            finalResult.add(q);
        }
        return finalResult;
    }


    // this method merges the posting lists from two documents

    private List<Posting> mergePosting(List<Posting> list1, List<Posting> list2) {
        List<Posting> mergeResult = new ArrayList<>();
        Posting p1, p2;
        int ptrL1 = 0;
        int ptrL2 = 0;

        while (ptrL1 < list1.size() && ptrL2 < list2.size()) {
            p1 =  list1.get(ptrL1);
            p2 =  list2.get(ptrL2);
            int p1Id = p1.getDocumentId();
            int p2Id = p2.getDocumentId();
            if (p1Id == p2Id) {
                List<Integer> indexPositionsDoc1 = p1.getPositions();
                List<Integer> indexPositionsDoc2 = p2.getPositions();

                List<Integer> tmp = mergePositions(indexPositionsDoc1, indexPositionsDoc2);
                if (tmp.size() > 0) {
                    Posting p=new Posting(p1Id);
                    for (int i:tmp){
                        p.addPosition(i);
                    }
                    mergeResult.add(p);
                }
                ptrL1++;
                ptrL2++;

            } else if (p1Id < p2Id) {
                ptrL1++;
            } else {
                ptrL2++;
            }
        }
        return mergeResult;
    }

    //This method merges the postions from the position list from two Postings
    private List<Integer> mergePositions(List<Integer> posList1, List<Integer> posList2) {

        List<Integer> mergePosResult = new ArrayList<>();
        int PtrL1 = 0, PtrL2 = 0;

        while (PtrL1 < posList1.size() && PtrL2 < posList2.size()) {
            int pos1 = posList1.get(PtrL1);
            int pos2 = posList2.get(PtrL2);

            if (pos1 + 1 == pos2) {
                mergePosResult.add(pos2);
                PtrL1++;
                PtrL2++;
            } else if (pos1 < pos2) {
                PtrL1++;
            } else {
                PtrL2++;
            }
        }
        return mergePosResult;
    }

    @Override
    public String toString() {
        return "\"" + String.join(" ", mTerms) + "\"";
    }
}
