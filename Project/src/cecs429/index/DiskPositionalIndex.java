package cecs429.index;

import cecs429.TermFrequency.ContextStrategy;

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

    private RandomAccessFile postingsRAF;
    private RandomAccessFile vocabRAF;
    private RandomAccessFile vocabTableRAF;
    int N;


    public  int getN(){return N;}

    DiskPositionalIndex(Path path, int N) throws FileNotFoundException {
        this.path=String.valueOf(path);
        vocabRAF = new RandomAccessFile( path + "/index/vocab.bin", "rw");

        postingsRAF = new RandomAccessFile( path + "/index/postings.bin","rw");

        vocabTableRAF = new RandomAccessFile(path+"/index/vocabTable.bin","rw");

        this.mInvertedIndexMap = new HashMap<>();
        this.N=N;

    }

    //TODO:
    @Override
    public List<Posting> getPostings(String term){
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
                prevdocIdGap =currentdocIdGap;
                //p.setmDocumentId(docId);

                int tft = postingsRAF.readInt();
                p.setTermFrequency(tft);
                int currentPosGap;
                int prevPosGap =0;
                List<Integer> positionList = new ArrayList<>();

                for(int termFreq = 0; termFreq < tft; termFreq++){
                    currentPosGap = postingsRAF.readInt();
                    positionList.add(currentPosGap+prevPosGap);
                    prevPosGap = currentPosGap;
                }
                p.setmPositions(positionList);
                postingsList.add(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return postingsList;
    }

    private long binarySearchVocab(String term) throws IOException {
        long vocabTableLength = (vocabTableRAF.length() / 16);
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

            if (mid == j && i != 0 && j != 0 && i != j) {
                nextVocabByte = vocabLength - currentVocabByte;
            } else {
                nextVocabByte = vocabTableRAF.readLong();
            }


            vocabRAF.seek(currentVocabByte);
            char[] vocabTerm = new char[(int) (nextVocabByte - currentVocabByte)];
            for (int termlength = 0; termlength < vocabTerm.length; termlength++) {
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
    }


    //TODO:
    @Override
    public List<String> getVocabulary() {
        return null;
    }

    @Override
    public int getVocabulorySize() {
        try {
            return (int)vocabTableRAF.length() /16;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  0;
    }

    //TODO:
    @Override
    public List<Posting> getPostingsWithPosition(String term) {
        return null;
    }
}
