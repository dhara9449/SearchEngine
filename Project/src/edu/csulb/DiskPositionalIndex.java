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

///Users/indumanimaran/Documents/SET/MobyDick10Chapters
public class DiskPositionalIndex {
    private static String PATH;
    private static long indexTime=0;
    private static String EXTENSION = ".txt";
    private  static  ArrayList<String> modes;

    private static DocumentCorpus newCorpus(Path directoryPath, String extension) {
        DocumentCorpus corpus;
        if (extension.contains("txt")) {
            corpus = DirectoryCorpus.loadTextDirectory(directoryPath, extension);
        } else {
            corpus = DirectoryCorpus.loadJsonDirectory(directoryPath, extension);
        }
        return corpus;
    }

    DiskPositionalIndex(){
        modes = new ArrayList<>();
        modes.add("");
        modes.add("boolean");
        modes.add("ranked");
    }

    private static Index newIndex(DocumentCorpus corpus,DiskIndexWriter diskIndexWriter,Path directoryPath) {
        final long startTime = System.currentTimeMillis();
        Index index= diskIndexWriter.indexCorpus(corpus,directoryPath);

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
        DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
        Index index = newIndex(corpus,diskIndexWriter,directoryPath);

        BooleanQueryParser parser = new BooleanQueryParser();
        String query;
        Scanner reader = new Scanner(System.in);
        String mode;
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
                            index = newIndex(corpus,diskIndexWriter,directoryPath);
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


    private static void queryPosting(BooleanQueryParser parser, DocumentCorpus corpus, Index index, String query)  {
        Scanner scanner = new Scanner(System.in);
        String reply = "y";
        String docName;
        int docId;
        String mode;

        System.out.println("Enter retrieval mode: 1.Boolean 2.Ranked");
        mode = scanner.nextLine();
        mode = modes.get(Integer.parseInt(mode));

        QueryComponent queryComponent = parser.parseQuery(query);

        List<Posting> postings = queryComponent.getPostings(index,mode);

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