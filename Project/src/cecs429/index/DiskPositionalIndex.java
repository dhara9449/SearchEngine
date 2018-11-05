package cecs429.index;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Implements an Index using a Map of term and list of postings.
 */
public class DiskPositionalIndex implements Index {

    private String path;
    /*    private InputStream vocabIS;
        private DataInputStream vocabDIS;
        private InputStream postingsIS;
        private InputStream vocabTableFIS;
        private File vocab;
        private File vocabTable;
        private DataInputStream vocabTableDIS;*/

    private RandomAccessFile postingsRAF;
    private RandomAccessFile vocabRAF;
    private RandomAccessFile vocabTableRAF;

    private  RandomAccessFile weightsRAF;
    int N;

    /*
               TODO:
               Modify DiskPositionalIndexer so it knows how to open this 􏰀le and skip to an appropriate location to
               read a 8-byte double for Ld. Ld values will be used when calculating ranked retrieval scores.

           */
    public  int getN(){return N;}


    public DiskPositionalIndex(Path path,int N) throws FileNotFoundException {
        this.path=String.valueOf(path);
/*        vocab = new File(path+ "/index/vocab.bin");
        vocabDIS = new DataInputStream(new FileInputStream(vocab));*/

        vocabRAF = new RandomAccessFile( path + "/index/vocab.bin", "rw");

        // postings=new File( path + "/index/postings.bin");
        // postingsIS = new DataInputStream(new FileInputStream(postings));
        postingsRAF = new RandomAccessFile( path + "/index/postings.bin","rw");

        // vocabTable =new File( path+"/index/vocabTable.bin");
        /*  vocabTableDIS = new DataInputStream(vocabTableFIS);*/
        vocabTableRAF = new RandomAccessFile(path+"/index/vocabTable.bin","rw");


    }


    /*
    * Returns  a lis of postings without information about the term position in a document
    */
    @Override
    public List<Posting> getPostings(String term) {
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
                postingsList.add(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return postingsList;    }

    //TODO:
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
    }

    //TODO:
    @Override
    public List<String> getVocabulary() {

        List<String> mVocabulary = new ArrayList<>();
        /*mVocabulary.addAll(mInvertedIndexMap.keySet());
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);*/
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


}
