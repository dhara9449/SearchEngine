package cecs429.index;

import java.util.ArrayList;
import java.util.List;

 /*
    Posting class asscociates the DocumentId with the query component.
 */

public class Posting {

    private int mDocumentId;
    private List<Integer> mPositions;
    private int  documentFrequency;

    public Posting(int documentId) {
        mPositions = new ArrayList<>();
        documentFrequency=0;
        mDocumentId = documentId;
    }

  //Gets document ID of the current posting object.
    public int getDocumentId() {
        return mDocumentId;
    }

  // Gets list of positions where the term occurs inside a given document
    public List<Integer> getPositions() {
        return mPositions;
    }

    //adds postion of the term in the document
    public void addPosition(int position) {
        mPositions.add(position);
        documentFrequency++;

    }

    public int getDocumentFrequency(){
        return  documentFrequency;
    }
}