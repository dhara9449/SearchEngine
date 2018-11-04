package cecs429.TermFrequency;

import java.util.List;

public class ContextStrategy {
    private TermFrequencyStrategy strategy;

    public ContextStrategy(TermFrequencyStrategy strategy){
        this.strategy = strategy;
    }

    public double calculateWqt(int num1, int num2){
        return strategy.calculateWqt(num1, num2);
    }

    public double calculateWdt(int num1){
        return strategy.calculateWdt(num1);
    }

    public double calculateLd(List<Integer> termFrequencies){
        return strategy.calculateLd(termFrequencies);
    }
}
