package cecs429.index;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DiskIndexWriter {

    public void WriteIndex(Index index, Path path) throws IOException {

        //path = D:\Dhara_MS_in_CS\Java_Projects\MobyDick10Chapters
        List<String> vocab = index.getVocabulary();
        ArrayList<Long> mMapPosting = writePostings(index,path,vocab);

        ArrayList<Long> mMapVocab = writeVocab(index, path, vocab);

        writeVocabTable(index,path,vocab,mMapPosting,mMapVocab);

    }

    public  ArrayList<Long> writePostings(Index index, Path path, List<String> vocab) throws IOException {
        //File postingsfile = new File(String.valueOf(path) + "\\index\\postings.bin");
        File postingsfile = new File(String.valueOf(path) + "/index/postings.bin");
        postingsfile.getParentFile().mkdirs();

        FileOutputStream out1 = new FileOutputStream(postingsfile);
        DataOutputStream postingsout = new DataOutputStream(out1);

        ArrayList<Long> mMapPosting = new ArrayList<>();

        for (String str : vocab) {
            int prevdocId;
            int currentdocId = 0;
            int docIdGap;
            for (Posting document : index.getPostings(str,"boolean")) {
                prevdocId = currentdocId;
                currentdocId = document.getDocumentId();


                docIdGap = currentdocId - prevdocId;
                postingsout.writeInt(docIdGap); //TODO: clarify... should it be docIdGap ot currentDocID


                // writing posting.bin
                int prevPosId;
                int currentPosId =0;
                postingsout.writeInt(document.getDocumentFrequency());

                //determing the location for vocab table
                mMapPosting.add(out1.getChannel().size());


                for (Integer position : document.getPositions()) {
                    prevPosId=currentPosId;
                    currentPosId = position;
                    postingsout.writeLong(currentPosId - prevPosId);
                }
            }
        }
        return mMapPosting;
    }

    public  ArrayList<Long> writeVocab(Index index, Path path, List<String> vocab) throws IOException {
       // File vocabfile = new File(String.valueOf(path) + "\\index\\vocab.bin");
        File vocabFile = new File(String.valueOf(path) + "/index/vocab.bin");
        FileOutputStream out2 = new FileOutputStream(vocabFile);
        DataOutputStream vocabOut = new DataOutputStream(out2);
        ArrayList<Long> mMapVocab = new ArrayList<>();

        for(String str: vocab){
            vocabOut.writeBytes(str);
            vocabOut.flush();
            mMapVocab.add(out2.getChannel().size());
        }
        return mMapVocab;
    }

    public void writeVocabTable(Index index, Path path, List<String> vocab, ArrayList<Long> mMapVocab,ArrayList<Long> mMapPosting) throws IOException {
        //File vocabTablefile = new File(String.valueOf(path) + "\\index\\vocabTable.bin");
        File vocabTablefile = new File(String.valueOf(path) + "/index/vocabTable.bin");
        DataOutputStream vocabtableout = null;
        try {
            vocabtableout = new DataOutputStream(new FileOutputStream(vocabTablefile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int i=0;i< mMapVocab.size();i++){
            vocabtableout.writeLong(mMapVocab.get(i));
            vocabtableout.writeLong(mMapPosting.get(i));
        }
    }

     private  void writeDocWeights(HashMap<Integer,Double> Ld,Path path) throws IOException{
         File docWeightsfile = new File(String.valueOf(path) + "/index/docWeights.bin");
         DataOutputStream docWeightsout = null;

         try {
             docWeightsout = new DataOutputStream(new FileOutputStream(docWeightsfile));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }

         for (HashMap.Entry<Integer,Double> hm:Ld.entrySet()){
             docWeightsout.writeDouble(hm.getValue());

         }

     }

    public  Index indexCorpus(DocumentCorpus corpus,Path path) {
        HashSet<String> vocabulary = new HashSet<>();
        BetterTokenProcessor processor = new BetterTokenProcessor();//must be dynamic
        EnglishTokenStream englishTokenStream;
        PositionalInvertedIndex invertedDocumentIndex = new PositionalInvertedIndex();

        HashMap<Integer,Double> docWeights =new HashMap<Integer, Double>();
        int corpusSize=corpus.getCorpusSize();
        int currentDocId;
        for (Document document : corpus.getDocuments()) {
            englishTokenStream = new EnglishTokenStream(document.getContent());
            Iterable<String> getTokens = englishTokenStream.getTokens();
            int position = 0;
            String lastTerm = "";
            String term;
            HashMap<String,Integer> termFrequencyTracker = new HashMap<>();;
            currentDocId = document.getId();
            int frequency;
            for (String tokens : getTokens) {
                for (String token : processor.processToken(tokens)) {
                    term = token;
                    if(!term.trim().equals("")) {
                        invertedDocumentIndex.addTerm(term, currentDocId, position);
                        if(termFrequencyTracker.containsKey(term)){
                            frequency=termFrequencyTracker.get(term);
                            termFrequencyTracker.put(term,frequency+1);
                        }else{
                            termFrequencyTracker.put(term,1);
                        }
                    }
                }
            }
            try {
                englishTokenStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            double Ld=  0.0;

            for (Integer tf : termFrequencyTracker.values()) {
                Ld = Ld+ Math.pow(1+Math.log(tf),2);
            }
            docWeights.put(currentDocId,Math.sqrt(Ld));
        }

        // write the index to disk
        try {
            WriteIndex(invertedDocumentIndex,path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write term document weight to disk

        try {
            writeDocWeights(docWeights,path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return invertedDocumentIndex;
        /*
            TODO:
            Modify DiskPositionalIndex so it knows how to open this 􏰀le and skip to an appropriate location to
            read a 8-byte double for Ld. Ld values will be used when calculating ranked retrieval scores.

        */
    }


}
