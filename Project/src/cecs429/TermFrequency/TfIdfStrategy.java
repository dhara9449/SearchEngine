package cecs429.TermFrequency;

import java.util.List;

public class TfIdfStrategy implements  TermFrequencyStrategy{
    public double calculateWqt(int N, int dft){
        return Math.log( N*1.0/dft);
    }
    public  double calculateWdt(String path,int tf,int docId){
        return tf;
    }
    public double calculateLd(String path,int docId) {
        double Ld = 0.0;
        //retreive  docId*8*4 from path
        return Ld;

    }
}
