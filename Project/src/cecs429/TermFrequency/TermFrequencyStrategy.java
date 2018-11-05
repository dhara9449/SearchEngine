package cecs429.TermFrequency;

import java.io.IOException;
import java.util.List;

public interface TermFrequencyStrategy {

    public double calculateWqt(int N, int dft);
    public  double calculateWdt(int tf,int docId) throws IOException;
    public  double calculateLd( int docId) throws IOException;

}
