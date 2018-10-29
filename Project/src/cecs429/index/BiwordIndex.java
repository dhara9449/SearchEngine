/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.index;

public final class BiwordIndex {

    private static Index index;

    private BiwordIndex() {

    }
    /*
     *   initialise the index
     */
    public  static void setIndex(Index index) {
        BiwordIndex.index = index;
    }

    public static Index getIndex() {
        return index;
    }

}
