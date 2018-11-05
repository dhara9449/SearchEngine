package cecs429.TermFrequency;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class TfIdfStrategy implements  TermFrequencyStrategy{

    private RandomAccessFile weightsRAF=null;

    public TfIdfStrategy(String path){
        try {
            weightsRAF = new RandomAccessFile(path+"/index/weights.bin","rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public double calculateWqt(int N, int dft){
        return Math.log( N*1.0/dft);
    }
    public  double calculateWdt(int tf,int docId){
        return tf;
    }
    public double calculateLd(int docId) throws IOException {
        weightsRAF.seek(docId*8*4);
        return  weightsRAF.readDouble();
    }
}
