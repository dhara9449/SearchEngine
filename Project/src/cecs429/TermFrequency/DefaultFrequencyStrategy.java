package cecs429.TermFrequency;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class DefaultFrequencyStrategy implements  TermFrequencyStrategy{
    private RandomAccessFile weightsRAF=null;

    public DefaultFrequencyStrategy(String path){
        try {
            weightsRAF = new RandomAccessFile(path+"/index/weights.bin","rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public double calculateWqt(int N, int dft) {
        return Math.log(1 + (N *1.0/ dft));
    }

    public double calculateWdt(int tf,int docId) {
        return 1 + Math.log(tf);
    }

    public double calculateLd(int docId) throws  IOException{
        double Ld = 0.0;
        weightsRAF.seek(docId*8*4);
        return  weightsRAF.readDouble();
    }
}
