package cecs429.query;

import cecs429.TermFrequency.ContextStrategy;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses boolean queries according to the base requirements of the CECS 429
 * project. Does not handle phrase queries, NOT queries, NEAR queries, or
 * wildcard queries... yet.
 */
public class RankedQueryParser {
    /**
     * Identifies a portion of a string with a starting index and a length.
     */

    ContextStrategy strategy ;
    int corpusSize;

    public RankedQueryParser(ContextStrategy strategy,int corpusSize){
        this.strategy = strategy;
        this.corpusSize = corpusSize;
    }
    private static class StringBounds {
        int start;
        int length;

        StringBounds(int start, int length) {
            this.start = start;
            this.length = length;
        }
    }

    /**
     * Encapsulates a QueryComponent and the StringBounds that led to its
     * parsing.
     */
    private static class Literal {

        StringBounds bounds;
        QueryComponent literalComponent;

        Literal(StringBounds bounds, QueryComponent literalComponent) {
            this.bounds = bounds;
            this.literalComponent = literalComponent;
        }
    }

    /**
     * Given a boolean query, parses and returns a tree of QueryComponents
     * representing the query.
     *
     * @param query
     * @return
     */
    public QueryComponent parseQuery(String query, TokenProcessor processor) {
        int start = 0;

        // General routine: scan the query to identify a literal, and put that literal into a list.
        //	Repeat until a + or the end of the query is encountered; build an AND query with each
        //	of the literals found. Repeat the scan-and-build-AND-query phase for each segment of the
        // query separated by + signs. In the end, build a single OR query that composes all of the built
        // AND subqueries.
        List<QueryComponent> allSubqueries = new ArrayList<>();
        do {
            // Identify the next subquery: a portion of the query up to the next + sign.
            StringBounds nextSubquery = findNextSubquery(query, start);
            // Extract the identified subquery into its own string.
            String subquery = query.substring(nextSubquery.start, nextSubquery.start + nextSubquery.length);
            int subStart = 0;

            // Store all the individual components of this subquery.
            List<QueryComponent> subqueryLiterals = new ArrayList<>();
            int adjustment=0;
            do {
                // Extract the next literal from the subquery.
                Literal lit = findNextLiteral(subquery, subStart,processor);
                adjustment =0;
                // Add the literal component to the conjunctive list.
                subqueryLiterals.add(lit.literalComponent);

                //In case of phrase literal, we need to increase the subStart by 1 to move past the end quote of the query.
                if(lit.literalComponent.getClass() == PhraseLiteral.class){
                    adjustment=1;
                }
                // Set the next index to start searching for a literal.

                subStart = lit.bounds.start + lit.bounds.length+adjustment;

            } while (subStart < subquery.length() - 1);

            // After processing all literals, we are left with a conjunctive list
            // of query components, and must fold that list into the final disjunctive list
            // of components.
            // If there was only one literal in the subquery, we don't need to AND it with anything --
            // its component can go straight into the list.

            allSubqueries.addAll(subqueryLiterals);

            start = nextSubquery.start + nextSubquery.length;
        } while (start < query.length());

        // After processing all subqueries, we either have a single component or multiple components
        // that must be combined with an OrQuery.


        return new RankedRetrieval(allSubqueries,strategy,corpusSize);
    }

    /**
     * Locates the start index and length of the next sub query in the given
     * query string, starting at the given index.
     */
    private StringBounds findNextSubquery(String query, int startIndex) {
        int lengthOut;

        // Find the start of the next subquery by skipping spaces and + signs.
        char test = query.charAt(startIndex);
        while (test == ' ' || test == '+') {
            test = query.charAt(++startIndex);
        }

        // Find the end of the next subquery.
        int nextPlus = query.indexOf('+', startIndex + 1);

        if (nextPlus < 0) {
            // If there is no other + sign, then this is the final subquery in the
            // query string.
            lengthOut = query.length() - startIndex;
        } else {
            // If there is another + sign, then the length of this subquery goes up
            // to the next + sign.

            // Move nextPlus backwards until finding a non-space non-plus character.
            test = query.charAt(nextPlus);
            while (test == ' ' || test == '+') {
                test = query.charAt(--nextPlus);
            }

            lengthOut = 1 + nextPlus - startIndex;
        }

        // startIndex and lengthOut give the bounds of the subquery.
        return new StringBounds(startIndex, lengthOut);
    }

    /**
     * Locates and returns the next literal from the given sub query string.
     */
    private Literal findNextLiteral(String subquery, int startIndex,TokenProcessor processor) {
        int subLength = subquery.length();
        int lengthOut;

        // Skip past white space.
        while (subquery.charAt(startIndex) == ' ') {
            ++startIndex;
        }

        //determine if it is a  phrase literal
        if (subquery.charAt(startIndex) == '\"') {
            ++startIndex;
            int nextQuote = subquery.indexOf('\"', startIndex);
            if (nextQuote < 0) {
                // No more literals in this subquery.
                return new Literal(
                        new StringBounds(-1, -1),
                        new TermLiteral("",processor)
                );
            } else {
                lengthOut = nextQuote - startIndex;
            }
            // This is a term literal containing a single term.
            String substring = subquery.substring(startIndex, startIndex + lengthOut);

            return new Literal(
                    new StringBounds(startIndex, lengthOut),
                    new PhraseLiteral(substring,processor)
            );
        } //determine if it is a  Near literal
        else if (subquery.charAt(startIndex) == '[') {
            ++startIndex;
            int nextBracket = subquery.indexOf(']', startIndex);
            if (nextBracket < 0) {
                // No more literals in this subquery.
                return new Literal(
                        new StringBounds(-1, -1),
                        new TermLiteral("",processor)
                );
            } else {
                lengthOut = nextBracket - startIndex;
            }
            // This is a term literal containing a single term.
            String substring = subquery.substring(startIndex, startIndex + lengthOut);

            return new Literal(
                    new StringBounds(startIndex, lengthOut),
                    new NearLiteral(substring)
            );
        } else {
            // Locate the next space to find the end of this literal.
            int nextSpace = subquery.indexOf(' ', startIndex);
            if (nextSpace < 0) {
                // No more literals in this subquery.
                lengthOut = subLength - startIndex;
            } else {
                lengthOut = nextSpace - startIndex;
            }
            // assume that the words in phrase literal are separated by spaces
            String substring = subquery.substring(startIndex, startIndex + lengthOut);
            //substring = processor.processToken(substring).get(0);

            // substring=processor.processToken(subquery);
            return new Literal(
                    new StringBounds(startIndex, lengthOut),
                    new TermLiteral(substring,processor)
            );
        }

    }
}
