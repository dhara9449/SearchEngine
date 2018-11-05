package cecs429.query;

import cecs429.TermFrequency.ContextStrategy;
import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class RankedRetrieval implements QueryComponent {

    private ContextStrategy strategy;
    private List<QueryComponent> mComponents;
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

        double Ld;

        class Accumulator implements Comparable<Accumulator> {
            private  int docId;
            private double Ad =0.0;
            private Accumulator(int docID) {
                this.docId = docID;
            }

            private double getAd() {
                return Ad;
            }

            private void setAd(double Ad){
                this.Ad = Ad;
            }
            public  int getDocId(){
                return docId;
            }
            @Override
            public int compareTo(Accumulator otherAd)
            {
                return Double.compare(Ad, otherAd.Ad);
            }
        }


        // max priority queue , i.e = larger value equals higher priority
        PriorityQueue<Accumulator> accumulatorQueue = new PriorityQueue<>(Collections.reverseOrder());

        HashMap<Integer,Accumulator> accumulatorHashMap = new HashMap<>();
        Accumulator accumulator ;

        //For each term t
        for (QueryComponent component : mComponents){
            temp = component.getPostings(index);
             dft= temp.size();
             wqt=strategy.calculateWqt(N,dft);  // calculate wqt

            for(Posting p:temp){ // for each document d in t's posting list
                tf = p.getTermFrequency();
                docId=p.getDocumentId();
                wdt=strategy.calculateWdt(tf,docId);
                if(accumulatorHashMap.containsKey(docId)){
                    accumulator = accumulatorHashMap.get(docId);
                }
                else{
                    accumulator = new Accumulator(docId);
                }
                Ad = accumulator.getAd() + wqt * wdt;
                accumulator.setAd(Ad);
                accumulatorHashMap.put(docId,accumulator);
            }
        }

        //For each non-aero Ad, divide A_d by L_d , where L_d is read from docWeights.bin file
        for (Accumulator accum : accumulatorHashMap.values()) {
            Ld=strategy.calculateLd(accum.getDocId());
            accum.setAd(accum.getAd()/Ld);
            accumulatorQueue.add(accum);
        }


        List<Integer> result = new ArrayList<>();
        Iterator priorityQIterator = accumulatorQueue.iterator();
        int cnt=0;
        while (priorityQIterator.hasNext()) {
            Accumulator s=(Accumulator) priorityQIterator.next();
            result.add(s.getDocId());
            cnt++;
            if(cnt>=10)
                break;
        }
        return temp;
    }

    @Override
    public String toString() {
        return String.join(" ", mComponents.stream().map(Object::toString).collect(Collectors.toList()));
    }
}
