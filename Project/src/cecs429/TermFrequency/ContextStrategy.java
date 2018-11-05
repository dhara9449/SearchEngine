package cecs429.TermFrequency;

import java.util.List;

public class ContextStrategy {


    private TermFrequencyStrategy strategy;
    private  String path;

    public ContextStrategy(TermFrequencyStrategy strategy,String  path){
        this.strategy = strategy;
        this.path = path;
 }

    public double calculateWqt(int N, int dft){
        return strategy.calculateWqt(N, dft);
    }

    public double calculateWdt(int tf,int docId){
        return strategy.calculateWdt(tf,docId);
    }

    public double calculateLd(int docId){
        return strategy.calculateLd(docId);
    }
}
