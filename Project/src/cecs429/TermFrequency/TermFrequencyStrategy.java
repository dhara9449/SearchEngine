package cecs429.TermFrequency;

import java.util.List;

public interface TermFrequencyStrategy {
    public double calculateWqt(int N, int dft);
    public  double calculateWdt(int tf);
    public  double calculateLd(List<Integer> termFrequencies);

}
