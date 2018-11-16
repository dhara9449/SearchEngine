package cecs429.index;

import java.util.ArrayList;
import java.util.List;

 /*
    Posting class asscociates the DocumentId with the query component.
 */

public class Posting {

    private int mDocumentId;
    private List<Integer> mPositions;
    private int  termFrequency;

    public Posting(int documentId) {
        mPositions = new ArrayList<>();

        mDocumentId = documentId;
    }


    //TO DO :: Another constrctor with termFreq
  //Gets document ID of the current posting object.
    public int getDocumentId() {
        return mDocumentId;
    }

  // Gets list of positions where the term occurs inside a given document
    public List<Integer> getPositions() {
        return mPositions;
    }

    //adds position of the term in the document
    public void addPosition(int position) {
        mPositions.add(position);
        termFrequency=termFrequency+1;

    }

    public int getTermFrequency(){
        return  termFrequency;

    }


    public void setmDocumentId(int mDocumentId) {
        this.mDocumentId = mDocumentId;
    }

    public void setmPositions(List<Integer> mPositions) {
        this.mPositions = mPositions;
    }

    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }

  }