package cecs429.query;

import cecs429.TermFrequency.ContextStrategy;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RankedRetrieval implements QueryComponent {
    private ContextStrategy strategy;
    private List<QueryComponent> mComponents;
    private  int N;
    private  boolean accum;
    DocumentCorpus corpus;
    private int numDocsReturned;
    RankedRetrieval(List<QueryComponent> components, ContextStrategy strategy, DocumentCorpus corpus, String accum, int numDocReturned) {
        mComponents = components;
        this.strategy = strategy;
        this.corpus = corpus;
        N = corpus.getCorpusSize();
        this.accum= accum.equalsIgnoreCase("log");
        this.numDocsReturned = numDocReturned;
    }

    //inner class for accumulator values
    private class Accumulator implements Comparable<Accumulator> {
        private  int docId;
        private double Ad =0.0;
        private  double wdt=0.0;

        private Accumulator(int docID) {
            this.docId = docID;
        }

        private  double getWdt(){
            return  wdt;
        }
        private  void setWdt(double x){
            wdt =x;
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
            return Double.compare(this.Ad, otherAd.Ad);
        }
    }

    @Override
    public List<Posting> getPostings(Index index) {
        List<Posting> temp = new ArrayList<>();
        int docId;
        int tf;
        int dft;
        double wdt=0.0,wqt;
        double Ad;
        double Ld=0.0;

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
                try {
                    wdt=strategy.calculateWdt(tf,docId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(accumulatorHashMap.containsKey(docId)){
                    accumulator = accumulatorHashMap.get(docId);
                }
                else{
                    accumulator = new Accumulator(docId);
                }
                Ad = accumulator.getAd() + wqt * wdt;
                accumulator.setAd(Ad);
                accumulator.setWdt(wdt);
                accumulatorHashMap.put(docId,accumulator);
            }
        }

        //For each non-zero Ad, divide A_d by L_d , where L_d is read from docWeights.bin file
        for (Accumulator accum : accumulatorHashMap.values()) {
            try {
                Ld=strategy.calculateLd(accum.getDocId());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (accum.getAd()!=0) {
                accum.setAd(accum.getAd() / Ld);
            }
            accumulatorQueue.add(accum);
        }

        ArrayList<Posting> ans=new ArrayList<>();
        Iterator priorityQIterator = accumulatorQueue.iterator();
        int count=0;
        String printResult="";

        while (priorityQIterator.hasNext()) {
            Accumulator s = (Accumulator) priorityQIterator.next();
            priorityQIterator.remove();
            int dId = s.getDocId();

            if (accum) {
                System.out.print("A:" + s.getAd() + "\tdocId: " + s.getDocId());
            }
            Document doc=corpus.getDocument(dId);
            try {
                if(accum) {
                    System.out.print("\tLd: " + strategy.calculateLd(dId));
                    System.out.println("\tWdt: " + s.getWdt());

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
             we print the results only for ranked Queries where numDocsReturned <50 as otherwise printing for more
             documents adds time expenses in terms of console I/O
            */
            if (numDocsReturned <50 && numDocsReturned >0) {
                printResult = printResult + "Document Title:\"" + doc.getTitle() + "\"  File Name: " + doc.getmFileName() + " (ID: " + dId + ") " + s.getAd() + "\n";

            }
            ans.add(new Posting(dId));
            count++;
            if (count>=numDocsReturned){
                break;
            }
        }
        /*
             we print the results only for ranked Queries where numDocsReturned <50 as otherwise printing for more
             documents adds time expenses in terms of console I/O
            */
        if (numDocsReturned <50 && numDocsReturned >0) {
            System.out.print(printResult);
        }
        return ans;
    }

    @Override
    public String toString() {
        return String.join(" ", mComponents.stream().map(Object::toString).collect(Collectors.toList()));
    }
}
