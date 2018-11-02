package cecs429.index;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

/**
 * Implements an Index using a Map of term and list of postings.
 */
public class DiskPositionalIndex implements Index {

    private Map<String, List<Posting>> mInvertedIndexMap;
    private String p;
    private InputStream vocabIS;
    private InputStream postingsIS;
    private InputStream vocabTableIS;
    private File vocab;
    private File postings;
    private File vocabTable;

    public DiskPositionalIndex(Path path) throws FileNotFoundException {
        p=String.valueOf(path);
        vocab = new File(p+ "/index/vocab.bin");
        vocabIS = new DataInputStream(new FileInputStream(vocab));

        postings=new File( p + "/index/postings.bin");
        postingsIS = new DataInputStream(new FileInputStream(postings));

        vocabTable =new File( p+"/index/vocabTable.bin");
        vocabTableIS = new DataInputStream(new FileInputStream(vocabTable));

        this.mInvertedIndexMap = new HashMap<>();
    }


    //TODO:
    @Override
    public List<Posting> getPostings(String term,String mode) {
        List<Posting> temp = new ArrayList<>();
        // get the position of postings from vocabTable.bin
        //using binary search ....

        int high = (int)vocabTable.length();
        int low = 0;
        int mid = (int)high/2;

        while(low<=high){
            vocabTable.

        }





        if (mode.equalsIgnoreCase("boolean")){
            if (mInvertedIndexMap.containsKey(term)) {
                return mInvertedIndexMap.get(term);
            }
        }else{
    //get a list of postings store it in temp

            for(Posting p:temp){

            }

        }

        return temp;
    }


    //TODO:
    @Override
    public List<String> getVocabulary() {
        List<String> mVocabulary = new ArrayList<>();
        mVocabulary.addAll(mInvertedIndexMap.keySet());
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);
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
