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
    private  int N;

    RankedRetrieval(List<QueryComponent> components,ContextStrategy strategy,int corpusSize) {
        mComponents = components;
        this.strategy = strategy;
        N = corpusSize;
    }


    @Override
    public List<Posting> getPostings(Index index){
        List<Posting> temp = new ArrayList<>();
        int docId;
        int tf;
        int dft;
        double wdt,wqt;
        double Ad;


        for (QueryComponent component : mComponents){
            temp = component.getPostings(index);
             dft= temp.size();

             wqt=strategy.calculateWqt(N,dft);

            for(Posting p:temp){
                tf = p.getTermFrequency();
                docId=p.getDocumentId();
                wdt=strategy.calculateWdt(tf,docId);

                Ad=0;
                if(Accumulator.containsKey(docId)){
                    Ad=Accumulator.get(docId);
                }
                Ad = Ad + wqt * wdt;
                Accumulator.put(docId,Ad);
            }


        }
        //read from disk

        return temp;
    }

    @Override
    public String toString() {
        return String.join(" ", mComponents.stream().map(Object::toString).collect(Collectors.toList()));
    }
}