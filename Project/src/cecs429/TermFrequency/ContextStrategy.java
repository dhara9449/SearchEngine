package cecs429.TermFrequency;

import java.io.IOException;

public class ContextStrategy {


    private TermFrequencyStrategy strategy;

    public ContextStrategy(TermFrequencyStrategy strategy){
        this.strategy = strategy;
<<<<<<< HEAD
    }
=======
        this.path = path;
 }
>>>>>>> ed445c75dd652ceab816b4e3a5387adef953bf92

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
