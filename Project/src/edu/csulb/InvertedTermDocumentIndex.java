/*
 * To change this license header, choose Licensex Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.*;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.BetterTokenProcessor;
import cecs429.text.EnglishTokenStream;
import java.io.File;
import java.io.IOException;
import static java.lang.Integer.min;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class InvertedTermDocumentIndex {
    private static String PATH;
    private static long indexTime=0;
    private static String EXTENSION = ".txt";

    private static DocumentCorpus newCorpus(Path directoryPath, String extension) {
        DocumentCorpus corpus;
        if (extension.contains("txt")) {
            corpus = DirectoryCorpus.loadTextDirectory(directoryPath, extension);
        } else {
            corpus = DirectoryCorpus.loadJsonDirectory(directoryPath, extension);
        }
        return corpus;
    }

    private static Index newIndex(DocumentCorpus corpus) {
        final long startTime = System.currentTimeMillis();

        Index index= indexCorpus(corpus);

        final long endTime = System.currentTimeMillis();
        indexTime = endTime-startTime;
        System.out.println("Time taken for indexing corpus:"+indexTime/1000 +" seconds");
        return index;

    }

    public static void main(String[] args) throws IOException {
        SnowballStemmer stemmer = new englishStemmer();
        Scanner scanner=new Scanner(System.in);
        System.out.println("Enter corpus path:");
        PATH=scanner.nextLine();
        Path directoryPath = Paths.get(PATH);
        System.out.println("Indexing..."+ directoryPath.toString());
        DocumentCorpus corpus;

        File folder2 = new File(PATH);
        File[] listOfFiles = folder2.listFiles();
        EXTENSION = FilenameUtils.getExtension(listOfFiles[0].getName());

        corpus = newCorpus(directoryPath,"."+ EXTENSION);
        Index index = newIndex(corpus);

        DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
        diskIndexWriter.WriteIndex(index,directoryPath);
        BooleanQueryParser parser = new BooleanQueryParser();
        String query;
        Scanner reader = new Scanner(System.in);
        OUTER:
        while (true) {
            System.out.print("Query : ");
            query = reader.nextLine();
            int len = query.split("\\s+").length;
            if (len == 1) {
                switch (query) {
                    case ":q":
                        break OUTER;
                    case ":vocab":
                        System.out.println("@vocabulary ");
                        List<String> vocabulary = index.getVocabulary();
                        List<String> newList = new ArrayList<>(vocabulary.subList(0, min(vocabulary.size(), 1000)));
                        newList.forEach((vocab) -> {
                            System.out.println(vocab);
                        });
                        System.out.println("#vocabulary terms: " + vocabulary.size());
                        break;

                    case ":biwordVocab":
                        Index biwordIndex=BiwordIndex.getIndex();
                        System.out.println("Biword Index size: "+ biwordIndex.getVocabulary().size());
                        break;
                    default:
                        queryPosting(parser, corpus, index, query);
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
                            EXTENSION = FilenameUtils.getExtension(listOfFiles2[0].getName());
                            corpus = newCorpus(tempPath,"."+ EXTENSION);
                            index = newIndex(corpus);
                        } else {
                            System.out.println("The specified directory does not exist");
                        }
                        break;
                    default:
                        queryPosting(parser, corpus, index, query);
                        break;
                }
            } else {
                queryPosting(parser, corpus, index, query);
            }
        }
    }

    private static Index indexCorpus(DocumentCorpus corpus) {
        HashSet<String> vocabulary = new HashSet<>();
        BetterTokenProcessor processor = new BetterTokenProcessor();
        EnglishTokenStream englishTokenStream;
        PositionalInvertedIndex invertedDocumentIndex = new PositionalInvertedIndex();

        PositionalInvertedIndex biwordIndex = new PositionalInvertedIndex();

        int corpusSize=corpus.getCorpusSize();
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

    private static void queryPosting(BooleanQueryParser parser, DocumentCorpus corpus, Index index, String query)  {

        Scanner scanner = new Scanner(System.in);
        EnglishTokenStream englishTokenStream;
        String reply = "y";
        String docName;
        int docId;

        QueryComponent queryComponent = parser.parseQuery(query);

        List<Posting> postings = queryComponent.getPostings(index);

        if (postings != null) {
            for (Posting p : postings) {
                if (p.getDocumentId() >= 0) {
                    docId = p.getDocumentId();
                    System.out.println(corpus.getDocument(docId).getmFileName());
                }
            }
            System.out.println(postings.size() + " document(s)");

            while (reply.equalsIgnoreCase("y")) {
                System.out.println("View document(y/n):");
                reply = scanner.nextLine();
                if (reply.equalsIgnoreCase("y")) {
                    System.out.println("Enter document name:");
                    docName = scanner.nextLine();
                    for (Posting p : postings) {
                        docId = p.getDocumentId();
                        if (corpus.getDocument(docId).getTitle().equalsIgnoreCase(docName)) {
                            englishTokenStream = new EnglishTokenStream(corpus.getDocument(docId).getContent());
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