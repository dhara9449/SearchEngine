package cecs429.TermFrequency;

import java.util.List;

public class TfIdfStrategy {
    public double calculateWqt(int N, int dft){

        return Math.log( N/dft);
    }
    public  double calculateWdt(int tf){
        return tf;
    }
    public double calculateLd(List<Integer> termFrequencies) {
        double Ld = 0.0;
        for (Integer tf : termFrequencies) {
            Ld = Ld + Math.pow(1 + Math.log(tf), 2);
        }
        return Ld;

    }
}
