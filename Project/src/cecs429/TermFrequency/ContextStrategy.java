package cecs429.TermFrequency;

import java.io.IOException;

public class ContextStrategy {


    private TermFrequencyStrategy strategy;



    public ContextStrategy(TermFrequencyStrategy strategy){
        this.strategy = strategy;
        this.path=path;
    }

    public double calculateWqt(int N, int dft){
        return strategy.calculateWqt(N, dft);
    }

    public double calculateWdt(int tf,int docId) throws IOException {
        return strategy.calculateWdt(tf,docId);
    }

    public double calculateLd(int docId) throws IOException {

        return strategy.calculateLd(docId);
    }
}
