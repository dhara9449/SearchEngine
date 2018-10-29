package cecs429.text;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * An EnglishTokenStream creates tokens by splitting on whitespace.
 */
public class EnglishTokenStream implements TokenStream {

    private final Reader mReader;

    public EnglishTokenStream(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        mReader.close();
    }

    private class EnglishTokenIterator implements Iterator<String> {

        private final Scanner mScanner;

        private EnglishTokenIterator() {
            // A Scanner automatically tokenizes text by splitting on whitespace. By composing a Scanner we don't have to
            // duplicate that behavior.
            mScanner = new Scanner(mReader);
        }

        @Override
        public boolean hasNext() {
            return mScanner.hasNext();
        }

        @Override
        public String next() {
            return mScanner.next();
        }
    }

    /**
     * Constructs an EnglishTokenStream to create tokens from the given Reader.
     *
     * @param inputStream
     */
    public EnglishTokenStream(Reader inputStream) {
        mReader = inputStream;
    }

    @Override
    public Iterable<String> getTokens() {
        // Fancy trick to convert an Iterator to an Iterable.
        return () -> new EnglishTokenIterator();
    }
}
