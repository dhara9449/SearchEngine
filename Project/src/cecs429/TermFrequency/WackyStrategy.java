package cecs429.TermFrequency;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WackyStrategy implements  TermFrequencyStrategy {

    private RandomAccessFile weightsRAF=null;

    public WackyStrategy(String path){
        try {
            weightsRAF = new RandomAccessFile(path+"/index/weights.bin","rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public double calculateWqt(int N, int dft){

        return Math.max(0,Math.log( (N-dft)*1.0/dft));
    }
    public  double calculateWdt(int tf,int docId) throws IOException {
        double nr = 1+Math.log(tf);
        weightsRAF.seek(docId*8*4+4*8);
        double avg_tf_td=weightsRAF.readDouble();
        double dr= 1+Math.log(avg_tf_td);
        return nr/dr;
    }
    public  double calculateLd(int docId) throws IOException {
        weightsRAF.seek(docId*8*4+3*8);
        return Math.sqrt(weightsRAF.readDouble());
    }
    
}
