package cecs429.TermFrequency;

import java.util.List;

public class ContextStrategy {
    private TermFrequencyStrategy strategy;

    public ContextStrategy(TermFrequencyStrategy strategy){
        this.strategy = strategy;
    }

    public double calculateWqt(int N, int dft){
        return strategy.calculateWqt(N, dft);
    }

    public double calculateWdt(String path,int tf,int docId){
        return strategy.calculateWdt(path,tf,docId);
    }

    public double calculateLd(String path,int docId){
        return strategy.calculateLd(path, docId);
    }
}
