package cecs429.index;

import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class DiskIndexWriter {

    private Path path;

    private void WriteIndex(Index index, Path path) throws IOException {
        //path = D:\Dhara_MS_in_CS\Java_Projects\MobyDick10Chapters
       List<String> vocab = index.getVocabulary();

        ArrayList<Long> mMapPosting = writePostings(index,path, vocab);
        ArrayList<Long> mMapVocab = writeVocab(index,path, vocab);
        writeVocabTable(path,mMapVocab,mMapPosting);
        this.path = path;
    }

    private static void createBtree(){


    }
    private ArrayList<Long> writePostings(Index index, Path path, List<String> vocab) throws IOException {
       File postingsFile = new File(String.valueOf(path) + "/index/postings.bin");
       final boolean mkdirs = postingsFile.getParentFile().mkdirs();

       if(!mkdirs){
           System.out.println("Cannot create postings.bin");
           return new ArrayList<>();
       }

        FileOutputStream out1 = new FileOutputStream(postingsFile);
        DataOutputStream postingsout = new DataOutputStream(out1);
        ArrayList<Long> mMapPosting = new ArrayList<>();
        DB vocabTabledb = DBMaker.fileDB(String.valueOf(path)  +"/index/BTreeDatabase.db").make();
        BTreeMap<String, Long> map = vocabTabledb.treeMap("map")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .create();

        for (String str : index.getVocabulary()) {
            List<Posting> docFrequency = index.getPostings(str);
            postingsout.writeInt(docFrequency.size());
            Long postingPos = out1.getChannel().size()-4;
            //System.out.println(str + postingPos);
            map.put(str,postingPos);

            //determining the location for vocab table
            mMapPosting.add(postingPos);

            // the first document should be the docId
            int prevDocId = 0;
            int currentDocId;

            for (Posting document : docFrequency) {

                currentDocId = document.getDocumentId();
                postingsout.writeInt( currentDocId - prevDocId);
                prevDocId = currentDocId;

                List<Integer> positionList = document.getPositions();
                // writing posting.bin
                //postingsout.writeInt(document.getTermFrequency());
                postingsout.writeInt(positionList.size());
                int prevPos=0;
                for (Integer currentPos : positionList ) {
                    postingsout.writeInt(currentPos - prevPos); //writeInt
                    prevPos=currentPos;
                }

            }
        }
        vocabTabledb.close();
        return mMapPosting;
    }

    private ArrayList<Long> writeVocab(Index index, Path path, List<String> vocab) throws IOException {
        //File vocabFile = new File(String.valueOf(path) + "\\index\\vocab.bin");
        File vocabFile = new File(String.valueOf(path) + "/index/vocab.bin");
        FileOutputStream out2 = new FileOutputStream(vocabFile);
        DataOutputStream vocabOut = new DataOutputStream(out2);
        ArrayList<Long> mMapVocab = new ArrayList<>();

        for(String str: vocab){
            vocabOut.writeBytes(str);
            vocabOut.flush();
            mMapVocab.add(out2.getChannel().size()-str.length());
        }
        return mMapVocab;
    }


    /*
    *   Writes the vocabulary table to  the  disk
    *
    * */

    private void writeVocabTable(Path path, ArrayList<Long> mMapVocab,ArrayList<Long> mMapPosting) throws IOException {
        //File vocabTablefile = new File(String.valueOf(path) + "\\index\\vocabTable.bin");
        File vocabTablefile = new File(String.valueOf(path) + "/index/vocabTable.bin");
        DataOutputStream vocabtableout = null;
        try {
            vocabtableout = new DataOutputStream(new FileOutputStream(vocabTablefile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int i=0;i< mMapVocab.size();i++){
            Objects.requireNonNull(vocabtableout).writeLong(mMapVocab.get(i));
            vocabtableout.writeLong(mMapPosting.get(i));
        }
    }


    /*
    *   Write document weights to the disk
    *
    */

     private  void writeDocWeights(ArrayList<Double> Ld,Path path) throws IOException{
         File docWeightsfile = new File(String.valueOf(path) + "/index/docWeights.bin");
         DataOutputStream docWeightsout = null;

         try {
             docWeightsout = new DataOutputStream(new FileOutputStream(docWeightsfile));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }

         double docLengthA=0.0;
         int pos=1;
         for (Double item:Ld){
             Objects.requireNonNull(docWeightsout).writeDouble(item);
             if (pos%4==0){
                 docLengthA = docLengthA + item;
             }
             pos = pos+1;
         }

         Objects.requireNonNull(docWeightsout).writeDouble((docLengthA * 4) / Ld.size());
     }



     /*index the corpus given
       * also find the document weight for each document in the corpus
    */
    public  Index indexCorpus(DocumentCorpus corpus,Path path) {
        HashSet<String> vocabulary = new HashSet<>();
        DiskPositionalIndex diskPositionalIndex = null;

        BetterTokenProcessor processor = new BetterTokenProcessor();//must be dynamic
        EnglishTokenStream englishTokenStream;
        PositionalInvertedIndex invertedDocumentIndex = new PositionalInvertedIndex();

        ArrayList<Double> docWeights =new ArrayList<>();
        int currentDocId;
        for (Document document : corpus.getDocuments()) {
            englishTokenStream = new EnglishTokenStream(document.getContent());
            Iterable<String> getTokens = englishTokenStream.getTokens();
            int position = 0;
            String term;
            HashMap<String,Integer> termFrequencyTracker = new HashMap<>();;
            currentDocId = document.getId();
            int frequency;
            for (String tokens : getTokens) {
                for (String token : processor.processToken(tokens)) {
                    term = token;
                    if(!term.trim().equals("")) {
                        invertedDocumentIndex.addTerm(term, currentDocId, position);

                        position = position+1;
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
            double tf_td=0.0;
            for (Integer tf : termFrequencyTracker.values()) {
                Ld = Ld+ Math.pow(1+Math.log(tf),2);
                tf_td = tf_td + tf;
            }
            docWeights.add(Math.sqrt(Ld)); // docWeights d
            docWeights.add(position+0.0); // docLength d -- TODO
            docWeights.add(document.getByteSize());//Determine the bytesize of the document
            docWeights.add(tf_td/termFrequencyTracker.size());//avg tf t,d
        }

        DiskPositionalIndex dIndex = null;
        try {
            // write the index to disk
            WriteIndex(invertedDocumentIndex,path);

            // write term document weight to disk
            writeDocWeights(docWeights,path);

            dIndex=new DiskPositionalIndex(path,corpus.getCorpusSize());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  dIndex;
    }


}
