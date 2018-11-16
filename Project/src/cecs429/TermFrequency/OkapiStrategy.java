package cecs429.TermFrequency;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OkapiStrategy implements  TermFrequencyStrategy{

    private RandomAccessFile weightsRAF=null;

    public OkapiStrategy(String path){
        try {
            weightsRAF = new RandomAccessFile(path+"/index/docWeights.bin","rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public double calculateWqt(int N, int dft){
        double dr= dft+0.5; //denominator
        double nr= N - dft+ 0.5; //numerator
        return Math.max(0.1, Math.log(nr/dr));
    }
    public  double calculateWdt(int tf,int docId) throws IOException {
        weightsRAF.seek(docId*8*4 + 8);
        double docLength_d= weightsRAF.readDouble();

        weightsRAF.seek(weightsRAF.length()-8);
        double docLength_A = weightsRAF.readDouble();//last 8 bytes in the filePath
        double nr= 2.2*tf;
        double dr= 1.2*(0.25+0.75*(docLength_d/docLength_A))+tf;
        return nr/dr;
    }
    public  double calculateLd(int docId){
        return 1.0;
    }
    
}
