package cecs429.index;

import java.io.*;
import java.nio.file.FileSystems;
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


    }


    //TODO:
    @Override
    public List<Posting> getPostings(String term,String mode){
        List<Posting> postingsList = new ArrayList<Posting>();
        // get the position of postings from vocabTable.bin
        //using binary search ....

        try {
            int length =  (vocabTableFIS.available()/16);
            System.out.println(length);
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

            return postingsList;
        }else{
            //get a list of postings store it in temp for ranked retrieval queries
            for(Posting p:postingsList){

                return postingsList;
            }

        }
        System.out.println(postingsList);
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

        // To be implemented...


        List<String> mVocabulary = new ArrayList<>();
        /*mVocabulary.addAll(mInvertedIndexMap.keySet());
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);*/
        return null;
    }
}
