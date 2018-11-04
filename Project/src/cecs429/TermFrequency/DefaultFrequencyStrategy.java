package cecs429.TermFrequency;

import java.util.List;

public class DefaultFrequencyStrategy implements  TermFrequencyStrategy{
    public double calculateWqt(int N, int dft) {
        return Math.log(1 + (N *1.0/ dft));
    }

    public double calculateWdt(String path,int tf,int docId) {
        return 1 + Math.log(tf);
    }

    public double calculateLd(String path,int docId) {
        double Ld = 0.0;
        //retreive  docId*8*4 from path
        return Ld;

    }
}
