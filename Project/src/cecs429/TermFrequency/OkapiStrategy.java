package cecs429.TermFrequency;

import java.util.List;

public class OkapiStrategy {
    public double calculateWqt(int N, int dft){
        double dr= dft+0.5;
        double nr=N - dft+ 0.5;
        return Math.max(0.1, Math.log(nr/dr));
    }
    public  double calculateWdt(int tf){
        return tf;// what us docLengthD and docLengthA
    }
    public  double calculateLd(List<Integer> termFrequencies){
        return 1.0;
    }
    
}
