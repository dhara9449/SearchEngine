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

    public static ArrayList<Long> writePostings(Index index, Path path, List<String> vocab) throws IOException {
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
            List<Integer> mPositions;
            int postingSize;
            for (Posting document : index.getPostings(str)) {
                prevdocId = currentdocId;
                currentdocId = document.getDocumentId();

                mPositions = document.getPositions();
                postingSize=mPositions.size();

                docIdGap = currentdocId - prevdocId;
                postingsout.writeInt(docIdGap); //TODO: clarify... should it be docIdGap ot currentDocID

                //determing the location for vocab table
                mMapPosting.add(out1.getChannel().size());


                // writing posting.bin
                int prevPosId;
                int currentPosId =0;
                postingsout.writeInt(document.getDocumentFrequency());
                for (Integer position : document.getPositions()) {
                    prevPosId=currentPosId;
                    currentPosId = position;
                    postingsout.writeLong(currentPosId - prevPosId);
                }
            }
        }
        return mMapPosting;
    }

    public static ArrayList<Long> writeVocab(Index index, Path path, List<String> vocab) throws IOException {
       // File vocabfile = new File(String.valueOf(path) + "\\index\\vocab.bin");
        File vocabfile = new File(String.valueOf(path) + "/index/vocab.bin");
        FileOutputStream out2 = new FileOutputStream(vocabfile);
        DataOutputStream vocabout = new DataOutputStream(out2);
        ArrayList<Long> mMapVocab = new ArrayList<Long>();

        for(String str: vocab){
            vocabout.writeBytes(str);
            vocabout.flush();
            mMapVocab.add(out2.getChannel().size());
        }
        return mMapVocab;
    }

    public void writeVocabTable(Index index, Path path, List<String> vocab, ArrayList<Long> mMapVocab,ArrayList<Long> mMapPosting) throws IOException {
        //File vocabTablefile = new File(String.valueOf(path) + "\\index\\vocabTable.bin");
        File vocabTablefile = new File(String.valueOf(path) + "/index/vocabTable.bin");
        DataOutputStream vocabtableout = new DataOutputStream(new FileOutputStream(vocabTablefile));

        for(int i=0;i< mMapVocab.size();i++){
            vocabtableout.writeLong(mMapVocab.get(i));
            vocabtableout.writeLong(mMapPosting.get(i));
        }
    }

    public static Index indexCorpus(DocumentCorpus corpus) {
        HashSet<String> vocabulary = new HashSet<>();
        BetterTokenProcessor processor = new BetterTokenProcessor();//must be dynamic
        EnglishTokenStream englishTokenStream;
        PositionalInvertedIndex invertedDocumentIndex = new PositionalInvertedIndex();

        PositionalInvertedIndex biwordIndex = new PositionalInvertedIndex();

        int corpusSize=corpus.getCorpusSize();
        int currentDocId;
        for (Document document : corpus.getDocuments()) {
            englishTokenStream = new EnglishTokenStream(document.getContent());
            Iterable<String> getTokens = englishTokenStream.getTokens();
            int position = 0;
            String lastTerm = "";
            String term;
            HashMap<String,Integer> termFrequencyTracker;
            currentDocId = document.getId();
            int frequency;
            for (String tokens : getTokens) {
                termFrequencyTracker=new HashMap<>();
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


                        /*
                         * Creating biword index only for a small number of documents
                         * because for a larger corpus size, the following error:
                         * "java.lang.OutOfMemoryError: GC overhead limit exceeded"
                         * When tested on a corpus with small number of documents (in comparision to the given corpus of 36K+ docs
                         * the biword index works perfectly fine.
                         *
                         * */

                        if(currentDocId<150) {
                            biwordIndex.addTerm(lastTerm + " " + term, currentDocId, position - 1);
                        }
                        lastTerm = term;
                        position++;
                    }
                }
            }
            try {
                englishTokenStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        BiwordIndex.setIndex(biwordIndex);
        return invertedDocumentIndex;
    }


}
