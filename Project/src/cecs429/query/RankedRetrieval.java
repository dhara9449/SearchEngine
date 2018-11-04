package cecs429.query;

import cecs429.TermFrequency.ContextStrategy;
import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class RankedRetrieval implements QueryComponent {

    private ContextStrategy strategy;
    private List<QueryComponent> mComponents;
    private HashMap<Integer,Double> Accumulator;

    RankedRetrieval(List<QueryComponent> components,ContextStrategy strategy) {
        mComponents = components;
        this.strategy = strategy;
        Accumulator = new HashMap<>();
    }

    @Override
    public List<Posting> getPostings(Index index){
        List<Posting> temp = new ArrayList<>();
        int dft=0;//read from disk

        double wqt=strategy.calculateWqt(N,dft);

            //for each document in posting
        int docId=0;// read from file
        int tf = 0; // read from file
        double wdt = strategy.calculateWdt(path,tf,docId);

        double Ad=0;
        if(Accumulator.containsKey(docId)){
                Ad=Accumulator.get(docId);
        }
        Ad = Ad + wqt * wdt;
        Accumulator.put(docId,Ad);

        return temp;
    }

    @Override
    public String toString() {
        return String.join(" ", mComponents.stream().map(Object::toString).collect(Collectors.toList()));
    }
}
