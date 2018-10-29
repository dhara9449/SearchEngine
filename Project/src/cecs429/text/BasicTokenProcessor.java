package cecs429.text;

/*
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */

import java.util.ArrayList;
import java.util.List;

public class BasicTokenProcessor implements TokenProcessor {
    @Override
    public List<String> processToken(String token) {
        ArrayList<String> tmp = new ArrayList<String>();

        //tmp.add(token.replaceAll("\\W", "").toLowerCase());
        tmp.add(token.toLowerCase());
        return tmp;
    }
}
