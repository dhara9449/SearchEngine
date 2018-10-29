package cecs429.index;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class DiskIndexWriter {

    public void WriteIndex(Index index, Path path) throws IOException {

        //path = D:\Dhara_MS_in_CS\Java_Projects\MobyDick10Chapters
        List<String> vocab = index.getVocabulary();
        HashMap<String,Long> mMapPosting = writePostings(index,path,vocab);

        HashMap<String,Long> mMapVocab = writeVocab(index, path, vocab);

        writeVocabTable(index,path,vocab,mMapPosting,mMapVocab);

    }

    public static HashMap<String,Long> writePostings(Index index, Path path, List<String> vocab) throws IOException {
        File postingsfile = new File(String.valueOf(path) + "\\index\\postings.bin");
        postingsfile.getParentFile().mkdirs();

        FileOutputStream out1 = new FileOutputStream(postingsfile);
        DataOutputStream postingsout = new DataOutputStream(out1);

        HashMap<String,Long> mMapPosting = new HashMap<String,Long>();

        for (String str : vocab) {
            int prevdocId;
            int currentdocId = 0;
            int docIdGap;
            for (Posting docId : index.getPostings(str)) {
                prevdocId = currentdocId;
                currentdocId = docId.getDocumentId();

                docIdGap = currentdocId - prevdocId;
                postingsout.writeInt(docIdGap);

                mMapPosting.put(str, out1.getChannel().size());
                int prevPosId;
                int currentPosId =0;
                for (Integer position : docId.getPositions()) {
                    prevPosId=currentPosId;
                    currentPosId = position;
                    postingsout.writeLong(currentPosId - prevPosId);
                }
            }
        }
        return mMapPosting;
    }

    public static HashMap<String,Long> writeVocab(Index index, Path path, List<String> vocab) throws IOException {
        File vocabfile = new File(String.valueOf(path) + "\\index\\vocab.bin");
        FileOutputStream out2 = new FileOutputStream(vocabfile);
        DataOutputStream vocabout = new DataOutputStream(out2);
        HashMap<String,Long> mMapVocab = new HashMap<String,Long>();

        for(String str: vocab){
            vocabout.writeBytes(str);
            vocabout.flush();
            mMapVocab.put(str,out2.getChannel().size());
        }
        return mMapVocab;
    }

    public void writeVocabTable(Index index, Path path, List<String> vocab, HashMap<String,Long> mMapVocab, HashMap<String,Long> mMapPosting) throws IOException {
        File vocabTablefile = new File(String.valueOf(path) + "\\index\\vocabTable.bin");
        DataOutputStream vocabtableout = new DataOutputStream(new FileOutputStream(vocabTablefile));

        for(String str:vocab){
            vocabtableout.writeLong(mMapVocab.get(str));
            vocabtableout.writeLong(mMapPosting.get(str));
        }
    }

}
