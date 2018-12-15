package cecs429.index;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Implements an Index using a Map of term and list of postings.
 */
public class DiskPositionalIndex implements Index {

    private String path;
    private RandomAccessFile postingsRAF;
    private RandomAccessFile vocabRAF;
    private RandomAccessFile vocabTableRAF;
    private  RandomAccessFile weightsRAF;
    DB vocabTabledb;
    BTreeMap<String, Long> map;
    int N;




    public DiskPositionalIndex(Path path,int N) throws FileNotFoundException {
        this.path=String.valueOf(path);
        vocabRAF = new RandomAccessFile( path + "/index/vocab.bin", "rw");
        postingsRAF = new RandomAccessFile( path + "/index/postings.bin","rw");
        vocabTableRAF = new RandomAccessFile(path+"/index/vocabTable.bin","rw");
        vocabTabledb = DBMaker.fileDB(new File(path +"/index/BTreeDatabase.db")).make();
        map = vocabTabledb.treeMap("map")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .open();

    }


    /*
     TODO:
     Modify DiskPositionalIndexer so it knows how to open this 􏰀le and skip to an appropriate location to
     read a 8-byte double for Ld. Ld values will be used when calculating ranked retrieval scores.
    */
    public  int getN(){return N;}

    /*
    * Returns  a list of postings without information about the term position in a document
    */
    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> postingsList = new ArrayList<>();
        // get the position of postings from vocabTable.bin
        //using binary search ....

        try {
            long postingPos = binarySearchVocab(term);
            postingsRAF.seek(postingPos);
            int dft=postingsRAF.readInt();
            Posting p;
            int currentdocIdGap;
            int prevdocIdGap =0;

            for( int docFreq =0; docFreq < dft; docFreq++){
                currentdocIdGap = postingsRAF.readInt();
                p = new Posting(prevdocIdGap+currentdocIdGap);
                prevdocIdGap = prevdocIdGap + currentdocIdGap;
                //p.setmDocumentId(docId);

                int tft = postingsRAF.readInt();
                for(int termFreq = 0; termFreq < tft; termFreq++) {
                    postingsRAF.readInt();
                }

                p.setTermFrequency(tft);
                postingsList.add(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            return postingsList;
        }


        return postingsList;
    }

    @Override
    public List<Posting> getPostingsWithPosition(String term){

        List<Posting> postingsList = new ArrayList<>();
        // get the position of postings from vocabTable.bin
        //using binary search ....

        try {
            // int length =  (vocabTableFIS.available()/16);
            long postingPos = binarySearchVocab(term);
            postingsRAF.seek(postingPos);
            int dft=postingsRAF.readInt();
            Posting p;
            int currentdocIdGap;
            int prevdocIdGap =0;

            for( int docFreq =0; docFreq < dft; docFreq++){

                currentdocIdGap = postingsRAF.readInt();
                p = new Posting(prevdocIdGap+currentdocIdGap);
                prevdocIdGap = prevdocIdGap + currentdocIdGap;
                //p.setmDocumentId(docId);

                int tft = postingsRAF.readInt();
                p.setTermFrequency(tft);
                int currentPosGap;
                int prevPosGap =0;
                List<Integer> positionList = new ArrayList<>();

                for(int termFreq = 0; termFreq < tft; termFreq++){
                    currentPosGap = postingsRAF.readInt();
                    positionList.add(currentPosGap+prevPosGap);
                    prevPosGap = currentPosGap +prevPosGap;
                }
                p.setmPositions(positionList);
                postingsList.add(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return postingsList;
    }

    private long binarySearchVocab(String term) {
        //TODO: make case for no word in the map
        return map.get(term);
    }

/*    private long binarySearchVocab(String term) throws IOException {
        long vocabTableLength =  (vocabTableRAF.length() /16);
        long vocabLength = vocabRAF.length();
        long i = 0;
        long j = vocabTableLength - 1;
        long currentPostingsPos = 0;
        while (i <= j) {
            long mid = (i + j) / 2;
            vocabTableRAF.seek(mid * 16);

            long currentVocabByte = vocabTableRAF.readLong();
            currentPostingsPos = vocabTableRAF.readLong();
            long nextVocabByte;

            if(mid == j && i==vocabTableLength-1 && j==vocabTableLength-1){
                nextVocabByte = vocabLength;
            }
            else {
                // for the last vocab byte, the posting position will be the byte length of the vocabTable.bin
                nextVocabByte  = vocabTableRAF.readLong();
            }

            vocabRAF.seek(currentVocabByte);
            char[] vocabTerm = new char[(int) (nextVocabByte - currentVocabByte)];
            for (int termlength = 0; termlength < vocabTerm.length ; termlength++) {
                vocabTerm[termlength] = (char) vocabRAF.readByte();
            }
            String retrievedVocabTerm = String.valueOf(vocabTerm);

            if (retrievedVocabTerm.equals(term)) {
                //currentPostingsPos = currentPostingsPos;
                break;
            } else if (retrievedVocabTerm.compareTo((term)) > 0) {
                j = mid - 1;
            } else {
                i = mid + 1;
            }
        }
        return currentPostingsPos;
    }*/

    @Override
    public List<String> getVocabulary() throws IOException {

        List<String> vocabList = null;
        vocabList.addAll(map.keySet());

        return vocabList ;
    }
/*
    public List<String> getVocabulary() throws IOException {
        List<String> vocabResultList = new ArrayList<>();
        long vocabTableLength = (vocabTableRAF.length()/16);
        long vocabLength = vocabRAF.length();
        int count =1,termLength;
        long currentVocabByte,currentPostingPosition, nextVocabByte;
        char[] vocabTerm;
       // while(count<=1000 && vocabTableLength >= count) { //TO DO for all....

           currentVocabByte = vocabTableRAF.readLong();
           // currentPostingPosition = vocabTableRAF.readLong();

            if((count) < vocabTableLength) {
                vocabTableRAF.seek(count*16);
                nextVocabByte = vocabTableRAF.readLong();
                vocabTableRAF.seek(count*16);
                termLength = (int) (nextVocabByte - currentVocabByte);

            }else{
                termLength = (int) (vocabLength - currentVocabByte);
            }
            vocabTerm = new char[termLength];

            for (int i =0 ; i<termLength; i++){
                vocabTerm[i] = (char) vocabRAF.readByte();

            }

            String retrievedTerm = String.valueOf(vocabTerm);
            vocabResultList.add(retrievedTerm);
            count++;
       // }
        return vocabResultList;
    }
*/

    //TODO:
    @Override
    public int getVocabulorySize() {
        int vocabSize = 0;
        try {
            vocabSize = (int)vocabTableRAF.length() /16;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vocabSize;
    }

}
