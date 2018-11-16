/*
 * To change this license header, choose Licensex Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.csulb;

import cecs429.TermFrequency.*;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.query.RankedQueryParser;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.TokenProcessor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Integer.min;

public class MainIndexer {

    public static void main(String[] args) {

        DocumentCorpus corpus;
        String query;
        DiskIndexWriter diskIndexWriter = new DiskIndexWriter();

        SnowballStemmer stemmer = new englishStemmer();
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Milestone 1 (Boolean queries, in-memory index)\n" +
                "2. Milestone 2 (Boolean and ranked queries, on-disk index)\n" +
                "3. Milestone 3 (Average Precision calculations)\n" +
                "4. Stemmer test");

        int milestoneChoice = scanner.nextInt();

        if(milestoneChoice ==1){

        } else if(milestoneChoice == 2) {
            System.out.println("Enter corpus path:");
            String PATH = scanner.next();
            //String PATH = "D:\\Dhara_MS_in_CS\\Java_Projects\\Corpus\\MobyDick10Chapters";
            Path directoryPath = Paths.get(PATH);
            String sPath = directoryPath.toString();

            File folder2 = new File(PATH);
            File[] listOfFiles = folder2.listFiles();
            String EXTENSION = FilenameUtils.getExtension(Objects.requireNonNull(listOfFiles)[0].getName());

            System.out.println("1.Build corpus" +
                    "\n2.Query corpus" +
                    "\nEnter choice: ");
            int choice = scanner.nextInt();
            corpus = newCorpus(directoryPath, "." + EXTENSION);

            if (choice == 1) {
                System.out.println("Indexing..." + directoryPath.toString());
                newIndex(corpus, diskIndexWriter, directoryPath);
                return;
            }

            Index index = loadIndex(corpus, diskIndexWriter, directoryPath);

            ArrayList<String> modes = new ArrayList<>();
            modes.add("");
            modes.add("boolean");
            modes.add("ranked");

            ArrayList<TermFrequencyStrategy> rankRetrievalStrategy = new ArrayList<>();
            rankRetrievalStrategy.add(new DefaultFrequencyStrategy(sPath));
            rankRetrievalStrategy.add(new TfIdfStrategy(sPath));
            rankRetrievalStrategy.add(new OkapiStrategy(sPath));
            rankRetrievalStrategy.add(new WackyStrategy(sPath));

            ContextStrategy strategy = new ContextStrategy(rankRetrievalStrategy.get(0));

            System.out.println(" 1.Boolean\n" +
                    " 2.Ranked\n" +
                    "Enter retrieval mode:");
            int mode = scanner.nextInt();
            String currentMode = modes.get(mode);

            if (currentMode.equalsIgnoreCase("ranked")) {
                System.out.println("1.Default\n" +
                        "2.tf-idf\n" +
                        "3.Okapi BM25\n" +
                        "4.Wacky\n" +
                        "Enter choice: ");
                strategy = new ContextStrategy(rankRetrievalStrategy.get(scanner.nextInt()));
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
                            queryPosting(corpus, index, query, currentMode, strategy);
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
                        case ":index":
                            System.out.println("Program Restarted");
                            Path tempPath = Paths.get(query.split("\\s+")[1]);

                            if (Files.exists(tempPath)) {
                                File folder = new File(query.split("\\s+")[1]);
                                File[] listOfFiles2 = folder.listFiles();
                                EXTENSION = FilenameUtils.getExtension(Objects.requireNonNull(listOfFiles2)[0].getName());
                                corpus = newCorpus(tempPath, "." + EXTENSION);
                                index = newIndex(corpus, diskIndexWriter, directoryPath);
                            } else {
                                System.out.println("The specified directory does not exist");
                            }
                            break;
                        default:
                            queryPosting(corpus, index, query, currentMode, strategy);
                            break;
                    }
                } else {
                    queryPosting(corpus, index, query, currentMode, strategy);

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

    private static Index newIndex(DocumentCorpus corpus,DiskIndexWriter diskIndexWriter,Path directoryPath) {
        final long startTime = System.currentTimeMillis();
        Index index= diskIndexWriter.indexCorpus(corpus,directoryPath);

        final long endTime = System.currentTimeMillis();
        long indexTime = endTime - startTime;
        System.out.println("Time taken for indexing corpus:"+ indexTime /1000 +" seconds");
        return index;

    }

    private static Index loadIndex(DocumentCorpus corpus,DiskIndexWriter diskIndexWriter,Path directoryPath) {
        return diskIndexWriter.loadCorpus(corpus,directoryPath);
    }

    private static void queryPosting( DocumentCorpus corpus, Index index, String query,String mode,ContextStrategy strategy)  {
        Scanner scanner = new Scanner(System.in);
        String reply = "y";
        String docName;
        int docId;

        TokenProcessor processor =new BetterTokenProcessor();

        QueryComponent queryComponent;
        if (mode.equalsIgnoreCase("boolean")){
            queryComponent = new BooleanQueryParser().parseQuery(query, processor);
        }else{
            queryComponent = new RankedQueryParser(strategy,corpus.getCorpusSize()).parseQuery(query, processor);
        }

        List<Posting> postings = queryComponent.getPostings(index);

        if (postings != null) {
            for (Posting p : postings) {
                if (p.getDocumentId() >= 0) {
                    docId = p.getDocumentId();
                    System.out.println("docId "+docId );
                    //2  System.out.println("COntent "+corpus.getDocument(docId).getByteSize());
                    System.out.println( corpus.getDocument(docId).getTitle()+ "  "+corpus.getDocument(docId).getmFileName());
                }
            }
            System.out.println(postings.size() + " document(s)");
            while (reply.equalsIgnoreCase("y")) {
                System.out.println("View document(y/n):");
                reply = scanner.next();
                if (reply.equalsIgnoreCase("y")) {
                    System.out.println("Enter document name:");
                    docName = scanner.next();
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
