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
    private String path;
    /*    private InputStream vocabIS;
        private DataInputStream vocabDIS;
        private InputStream postingsIS;
        private InputStream vocabTableFIS;
        private File vocab;
        private File vocabTable;
        private DataInputStream vocabTableDIS;*/
    private InputStream vocabTableFIS;
    private RandomAccessFile postingsRAF;
    private RandomAccessFile vocabRAF;
    private RandomAccessFile vocabTableRAF;

    public DiskPositionalIndex(Path path) throws FileNotFoundException {
        this.path=String.valueOf(path);
/*        vocab = new File(path+ "/index/vocab.bin");
        vocabDIS = new DataInputStream(new FileInputStream(vocab));*/
        vocabRAF = new RandomAccessFile( path + "/index/vocab.bin", "rw");

        // postings=new File( path + "/index/postings.bin");
        // postingsIS = new DataInputStream(new FileInputStream(postings));
        postingsRAF = new RandomAccessFile( path + "/index/postings.bin","rw");

       // vocabTable =new File( path+"/index/vocabTable.bin");
        vocabTableFIS = new FileInputStream(new File( path+"/index/vocabTable.bin"));
        /*  vocabTableDIS = new DataInputStream(vocabTableFIS);*/
        vocabTableRAF = new RandomAccessFile(path+"/index/vocabTable.bin","rw");

        this.mInvertedIndexMap = new HashMap<>();
    }


    //TODO:
    @Override
    public List<Posting> getPostings(String term,String mode){
        List<Posting> postingsList = new ArrayList<Posting>();
        // get the position of postings from vocabTable.bin
        //using binary search ....

        try {
            int length =  (vocabTableFIS.available()/16);
            long postingPos = binarySearchVocab(term,0,length-1);
            postingsRAF.seek(postingPos);
            int dft=postingsRAF.readInt();
            Posting p;
            int currentdocIdGap;
            int prevdocIdGap =0;

            for( int docFreq =0; docFreq < dft; docFreq++){

                currentdocIdGap = postingsRAF.readInt();
                prevdocIdGap =currentdocIdGap;
                p = new Posting(prevdocIdGap+currentdocIdGap);
                //p.setmDocumentId(docId);

                int tft = postingsRAF.readInt();
                p.setTermFrequency(tft);
                int currentPosGap;
                int prevPosGap =0;
                List<Integer> positionList = new ArrayList<>();

                for(int termFreq = 0; termFreq < tft; termFreq++){
                    currentPosGap = postingsRAF.readInt();
                    prevPosGap = currentPosGap;
                    positionList.add(currentPosGap+prevPosGap);
                }
                p.setmPositions(positionList);
                postingsList.add(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mode.equalsIgnoreCase("boolean")){
            if (mInvertedIndexMap.containsKey(term)) {
                return mInvertedIndexMap.get(term);
            }
        }else{
            //get a list of postings store it in temp for ranked retrieval queries
            for(Posting p:postingsList){

            }

        }

        return postingsList;
    }

    private long binarySearchVocab(String term,int min, int max) throws IOException {
        // int length =  (vocabTableFIS.available()/16);
        //System.out.println(length);
        int i =min;
        int j = max;

        if(i>j){
            return -1;
        }

        //while(i<=j){
        long mid = (i+j)/2;
        // byte bs[] = new byte[length];
        long postingsPos=0;
        vocabTableRAF.seek(mid*16);
        long currentVocabByte = vocabTableRAF.readLong();
        //vocabTableDIS.skipBytes(1);
        long currentPostingsPos = vocabTableRAF.readLong();
        long nextVocabByte = vocabTableRAF.readLong();

        char[] vocabTerm =null;
        int pos=0;
        for(int termlength=0; termlength< nextVocabByte - currentVocabByte ; termlength++){
            vocabRAF.seek(currentVocabByte);
            vocabTerm[pos] = (vocabRAF.readChar());
            pos++;
        }
        String retrievedVocabTerm = vocabTerm.toString();

        if(retrievedVocabTerm.equals(term)){
            return currentPostingsPos;
        }
        else if(retrievedVocabTerm.compareTo(term)>0){
            return binarySearchVocab(term,i, j-1);
        }
        else{
            return binarySearchVocab(term,j+1,j);
        }
        //}
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
