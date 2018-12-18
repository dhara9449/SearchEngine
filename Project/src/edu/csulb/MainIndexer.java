/*
 * To change this license header, choose Licensex Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csulb;

import cecs429.TermFrequency.*;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.query.RankedQueryParser;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Integer.min;

public class MainIndexer {

    private  static  double TOTALTIME=0;
    private  static  int NQUERIES=0;
    public static void main(String[] args) {

        DocumentCorpus corpus ;
        TokenProcessor processor = new BetterTokenProcessor();

        String query;
        Index index ;
        DiskIndexWriter diskIndexWriter ;
        SnowballStemmer stemmer = new englishStemmer();
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Milestone 1 (Boolean queries, in-memory index)\n" +
                "2. Milestone 2 (Boolean and ranked queries, on-disk index)\n" +
                "3. Milestone 3\n");

        int milestoneChoice = scanner.nextInt();
        System.out.println("Enter corpus path:");
        String PATH = scanner.next();
        Path directoryPath = Paths.get(PATH);
        String sPath = directoryPath.toString();
        File folder2 = new File(PATH);
        File[] listOfFiles = folder2.listFiles();
        String EXTENSION = FilenameUtils.getExtension(Objects.requireNonNull(listOfFiles)[0].getName());
        corpus = newCorpus(directoryPath, "." + EXTENSION);

        String currentMode ="boolean";
        ArrayList<String> modes = new ArrayList<>();
        modes.add("");
        modes.add("boolean");
        modes.add("ranked");

        ArrayList<TermFrequencyStrategy> rankRetrievalStrategy = new ArrayList<>();

        ContextStrategy strategy = null;//new ContextStrategy(new DefaultFrequencyStrategy(""));

        if (milestoneChoice == 1) {
            System.out.println("Indexing..." + directoryPath.toString());
            index=newIndex(corpus, milestoneChoice, directoryPath,processor);
        }
        else {

            if (milestoneChoice == 2) {

                System.out.println("1.Build corpus" +
                        "\n2.Query corpus" +
                        "\nEnter choice: ");
                int choice = scanner.nextInt();

                if (choice == 1) {
                    System.out.println("Indexing..." + directoryPath.toString());
                    newIndex(corpus, milestoneChoice, directoryPath, processor);
                    return;
                }else{
                    System.out.println(" 1.Boolean\n" +
                            " 2.Ranked\n" +
                            "Enter retrieval mode:");
                    int mode = scanner.nextInt();
                    currentMode = modes.get(mode);
                }
            }
            rankRetrievalStrategy.add(new DefaultFrequencyStrategy(sPath));
            rankRetrievalStrategy.add(new TfIdfStrategy(sPath));
            rankRetrievalStrategy.add(new OkapiStrategy(sPath));
            rankRetrievalStrategy.add(new WackyStrategy(sPath));

            diskIndexWriter = new DiskIndexWriter();
            index = loadIndex(corpus, diskIndexWriter, directoryPath);

            if (currentMode.equalsIgnoreCase("ranked")) {
                System.out.println("1.Default\n" +
                        "2.tf-idf\n" +
                        "3.Okapi BM25\n" +
                        "4.Wacky\n" +
                        "Enter choice: ");
                strategy = new ContextStrategy(rankRetrievalStrategy.get(scanner.nextInt() - 1));
            }

            if (milestoneChoice == 3) {

                    System.out.println("1.Default\n" +
                            "2.tf-idf\n" +
                            "3.Okapi BM25\n" +
                            "4.Wacky\n" +
                            "Enter choice: ");

                    strategy = new ContextStrategy(rankRetrievalStrategy.get(scanner.nextInt() - 1));


                    while(true) {
                        System.out.println("1.MAP\n" +
                                "2.Throughput for 1 query\n" +
                                "Enter choice: ");

                        int choice = scanner.nextInt();
                        if (choice == 1) {
                            System.out.println("MAP: " + MeanAvgPrecision(corpus, index, strategy, "MAP"));
                            System.out.println("ThroughPut: " + NQUERIES / (TOTALTIME / 1000) + " q/s");
                            System.out.println("MRT: " + TOTALTIME / NQUERIES + " ms");
                        } else if (choice == 2) {
                            MeanAvgPrecision(corpus, index, strategy, "Throughput");
                            System.out.println("ThroughPut: " + NQUERIES / (TOTALTIME / 1000) + " q/s");
                        }else{break;}
                    }
                       return;
                    }
            }

        query = scanner.nextLine();

            OUTER:
            while (true) {
                System.out.print("Query : ");
                query = scanner.nextLine();

                int len = query.split("\\s+").length;
                if (len == 1) {
                    switch (query) {
                        case ":q":
                            break OUTER;
                        case ":vocab":
                            System.out.println("@vocabulary ");
                            List<String> vocabulary = null;
                            try {
                                vocabulary = index.getVocabulary();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            List<String> newList = new ArrayList<>(vocabulary.subList(0, min(Objects.requireNonNull(vocabulary).size(), 1000)));
                            newList.forEach(System.out::println);
                            System.out.println("#vocabulary terms: " + vocabulary.size());
                            break;

                        case ":biwordVocab":
                            Index biwordIndex = BiwordIndex.getIndex();
                            try {
                                System.out.println("Biword Index size: " + biwordIndex.getVocabulary().size());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:

                            //prints the log information like wdt, Ld
                            if (query.contains("--log")){
                                query=query.substring(0,query.lastIndexOf("--log"));
                                queryPosting(corpus, index, query,currentMode,strategy,"log");
                            }else{
                                queryPosting(corpus, index, query,currentMode,strategy,"default");
                            }
                            break;
                    }
                } else if (len == 2) {
                    String firstTerm = query.split("\\s+")[0];
                    switch (firstTerm) {
                        case ":stem":

                            String input = query.split("\\s+")[1];
                            stemmer.setCurrent(input); //set string you need to stem
                            stemmer.stem();  //stem the word
                            System.out.println(stemmer.getCurrent());
                            break;
                        default:
                            if (query.contains("--log")){
                                query=query.substring(0,query.lastIndexOf("--log"));
                                queryPosting(corpus, index, query,currentMode,strategy,"log");
                            }else{
                                queryPosting(corpus, index, query,currentMode,strategy,"default");
                            }
                            break;
                    }
                } else {
                    if (query.contains("--log")){
                        query=query.substring(0,query.lastIndexOf("--log"));
                        queryPosting(corpus, index, query,currentMode,strategy,"log");
                    }else{
                        queryPosting(corpus, index, query,currentMode,strategy,"default");
                    }

                }
            }
        }


    private static DocumentCorpus newCorpus(Path directoryPath, String extension) {
        DocumentCorpus corpus;
        if (extension.contains("txt")) {
            corpus = DirectoryCorpus.loadTextDirectory(directoryPath, extension);
        } else {
            corpus = DirectoryCorpus.loadJsonDirectory(directoryPath, extension);
        }
        return corpus;
    }

    private static Index newIndex(DocumentCorpus corpus,int milestoneChoice,Path directoryPath,TokenProcessor processor) {
        final long startTime = System.currentTimeMillis();
        Index index;
        if (milestoneChoice == 1) {
            PositionalInvertedIndex positionalIndex = new PositionalInvertedIndex();
            index = indexCorpus(corpus);

        }
        else {
            DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
            index = diskIndexWriter.indexCorpus(corpus, directoryPath,processor);
        }
        final long endTime = System.currentTimeMillis();
        long indexTime = endTime - startTime;
        System.out.println("Time taken for indexing corpus:" + indexTime / 1000 + " seconds");
        return index;
    }

    private static Index loadIndex(DocumentCorpus corpus,DiskIndexWriter diskIndexWriter,Path directoryPath) {
        return diskIndexWriter.loadCorpus(corpus,directoryPath);
    }

    //create the PositionalInvertedIndex
    private static Index indexCorpus(DocumentCorpus corpus) {
        HashSet<String> vocabulary = new HashSet<>();
        BetterTokenProcessor processor = new BetterTokenProcessor();
        EnglishTokenStream englishTokenStream;
        PositionalInvertedIndex invertedDocumentIndex = new PositionalInvertedIndex();

        PositionalInvertedIndex biwordIndex = new PositionalInvertedIndex();

        int currentDocId;
        for (Document document : corpus.getDocuments()) {
            englishTokenStream = new EnglishTokenStream(document.getContent());
            Iterable<String> getTokens = englishTokenStream.getTokens();
            int position = 0;
            String lastTerm = "";
            String term;
            currentDocId = document.getId();
            for (String tokens : getTokens) {
                for (String token : processor.processToken(tokens)) {
                    term = token;
                    if(!term.trim().equals("")) {
                        invertedDocumentIndex.addTerm(term, currentDocId, position);


                        /*
                         * Creating biword index only for a small number of documents
                         * because for a larger corpus size, the following error:
                         * "java.lang.OutOfMemoryError: GC overhead limit exceeded"
                         * When tested on a corpus with small number of documents (in comparision to the given corpus of 36K+ docs
                         * the biword index works perfectly fine.
                         *
                         * */


                        if(currentDocId<150) {
                            biwordIndex.addTerm(lastTerm + " " + term, currentDocId, position - 1);
                        }
                        lastTerm = term;
                        position++;
                    }
                }
            }
            try {
                englishTokenStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        BiwordIndex.setIndex(biwordIndex);
        return invertedDocumentIndex;
    }


    private  static  void setTime(double time){

        if( time!=-1) {
            TOTALTIME = TOTALTIME + time;
        }else{
            TOTALTIME=0;
        }

    }


    private static List<Posting> getPostings(DocumentCorpus corpus, Index index, String query,ContextStrategy strategy,String accum){

        TokenProcessor processor = new BetterTokenProcessor();
        QueryComponent queryComponent = new RankedQueryParser(strategy, corpus).parseQuery(query, processor, accum);

        Long startTime = System.currentTimeMillis();
        List<Posting> postings = queryComponent.getPostings(index);
        Long endTime = System.currentTimeMillis();
        setTime(endTime-startTime);
        return postings;

    }

    private static double avgPrecision( DocumentCorpus corpus, Index index, String query,ContextStrategy strategy,String accum,List<Integer> resultDocIds) {

        List<Posting> postings = getPostings(corpus,index,query,strategy,accum);
        StringBuilder sb= new StringBuilder();


        double avgPrecision = 0.0;
        double pAti=0.0;
        Posting p1;
        int docName;
        int docId;
        int postingSize=0;
        if (postings != null) {
            postingSize=postings.size();
            for (int i = 1; i <= postings.size(); i++) {
                p1 = postings.get(i-1);
                docId = p1.getDocumentId();
                if (docId >= 0) {
                    docName = Integer.parseInt(corpus.getDocument(docId).getmFileName().split("\\.")[0]);
                    if (resultDocIds.contains(docName)){
                        pAti ++;
                        avgPrecision = avgPrecision + pAti/i;
                        System.out.println(corpus.getDocument(docId).getmFileName() +" Relevant");
                    }else{
                        System.out.println(corpus.getDocument(docId).getmFileName() +" Not Relevant");
                    }
                    sb.append(pAti/i).append(" ");//precision
                    sb.append(pAti/postingSize).append(" ");//recall
                }
            }

        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(query + ".txt"));
            writer.write(sb.toString());

            writer.close();
        }catch (Exception e){
            System.out.println(e);
        }
        if (pAti==0)
            return 0;
        return avgPrecision/pAti;

    }

    private  static  void setNQueries(int N){
        NQUERIES = N;
    }

    private  static  double MeanAvgPrecision(DocumentCorpus corpus,Index index,ContextStrategy strategy,String mode){

            setNQueries(0);
            String query;
            Scanner scanner= new Scanner(System.in);
            System.out.println("Enter path to query file: ");
            String QPATH = scanner.nextLine();
            String RPATH = QPATH+"\\qrel";
            Double avgP;
            int nQueries=0;
            double time=0.0;
            double MAP=0.0;
            String result;
            BufferedReader reader,resultReader;
            try {
                reader = new BufferedReader(new FileReader(QPATH ));
                resultReader = new BufferedReader(new FileReader(RPATH));
                List<String> resultArray;
                setTime(-1);
                if (mode.equalsIgnoreCase("MAP")){
                    while ((query = reader.readLine()) != null && (result = resultReader.readLine()) != null) {
                        resultArray = Arrays.asList(result.split(" "));
                        List<Integer> intList = new ArrayList<>();
                        for (String s : resultArray) {
                            try {
                                intList.add(Integer.parseInt(s));
                            } catch (Exception e) {
                            }
                        }

                        Long startTime = System.currentTimeMillis();
                        avgP = avgPrecision(corpus, index, query, strategy, "default", intList);
                        Long endTime = System.currentTimeMillis();

                        time=time+endTime-startTime;
                        MAP = MAP + avgP;
                        System.out.println("\n"+query);
                        System.out.println("AVERAGE PRECISION: " + avgP);
                        nQueries++;

                    }
            }else {
                    System.out.println("Enter query no: ");
                    int lineNum = scanner.nextInt();
                    int j=0;
                    if ((query = reader.readLine()) != null) {
                        if(j+1==lineNum) {
                            Long startTime = System.currentTimeMillis();
                            for (int i = 0; i <= 30; i++) {
                                getPostings(corpus, index, query, strategy, "default");
                            }
                            Long endTime = System.currentTimeMillis();
                            setTime(endTime - startTime);
                            setNQueries(30);
                            nQueries = NQUERIES;
                        }
                        j++;
                    }
                }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setTime(time);
        setNQueries(nQueries);
            if (nQueries==0)
                return  0;
        return  MAP/nQueries;

    }
        private static void queryPosting( DocumentCorpus corpus, Index index, String query,String mode,ContextStrategy strategy,String accum)  {
        Scanner scanner = new Scanner(System.in);
        String reply = "y";
        String docName;
        int docId;

        TokenProcessor processor =new BetterTokenProcessor();

        QueryComponent queryComponent;
        if (mode.equalsIgnoreCase("boolean")){

            queryComponent = new BooleanQueryParser().parseQuery(query, processor,accum);
        }else{
            queryComponent = new RankedQueryParser(strategy,corpus).parseQuery(query, processor,accum);
        }

        List<Posting> postings = queryComponent.getPostings(index);
        if(mode.equalsIgnoreCase("boolean")) {

            Posting p1;
            if (postings != null) {
            for(int i=0;i<postings.size();i++){

                p1 = postings.get(i);
                if (p1.getDocumentId() >= 0) {
                    docId = p1.getDocumentId();
                    Document doc=corpus.getDocument(docId);
                    System.out.println("Document Title \""+doc.getTitle()+"\"  File Name: "+doc.getmFileName()+" (ID: "+docId+")");
                }
            }
                System.out.println(postings.size() + " document(s)");
            }
            while (reply.equalsIgnoreCase("y")) {
                System.out.println("View document(y/n):");
                reply = scanner.nextLine();
                if (reply.equalsIgnoreCase("y")) {
                    System.out.println("Enter document name:");
                    docName = scanner.nextLine();
                    for (Posting p : postings) {
                        docId = p.getDocumentId();
                        if (corpus.getDocument(docId).getmFileName().equalsIgnoreCase(docName)) {
                            try {
                                System.out.println(IOUtils.toString(corpus.getDocument(docId).getContent()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }


        }

    }
}
