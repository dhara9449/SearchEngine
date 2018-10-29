package cecs429.text;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BetterTokenProcessor implements TokenProcessor {

    private List<String> processedTokens;
    SnowballStemmer stemmer = new englishStemmer();
    @Override
    public List<String> processToken(String token) {

        List<String> temp = new ArrayList<>();
        processedTokens = new ArrayList<>();

        //removes all non alphanumeric characters from the beginning and ending of the token
        String processedToken1 = token.replaceAll("^[^\\sa-zA-Z0-9]+|[^a-zA-Z0-9\\s]+$", "");

        //removes all the single and double quotation marks from the token
        String processedToken2 = processedToken1.replaceAll("\'+|\"+", "");

        //lower cases the token
        String processedToken3 = processedToken2.toLowerCase();

        //normalizes the '-' in a token
        if (processedToken3.contains("-")) {
            temp.add(processedToken3.replaceAll("-", ""));
            StringTokenizer st = new StringTokenizer(processedToken3, "-");
            while (st.hasMoreTokens()) {
                String a = st.nextToken();
                if(!a.trim().equals(""))
                temp.add(a);
            }

        } else {
            temp.add(processedToken3);
        }

        //  stem all tokens
        for (String str : temp) {
            stemmer.setCurrent(str); //set string you need to stem
            stemmer.stem();
            processedTokens.add(stemmer.getCurrent());
        }
        return processedTokens;
    }

}
