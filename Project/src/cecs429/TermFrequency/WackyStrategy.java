package cecs429.TermFrequency;

import java.util.List;

public class WackyStrategy {
    public double calculateWqt(int N, int dft){

        return Math.max(0,Math.log( (N-dft)/dft));
    }
    public  double calculateWdt(int tf){

        double nr = 1+Math.log(tf);
        double dr= 1+Math.log(tf);// not right - refer formula
        return nr/dr;
    }
    public  double calculateLd(List<Integer> termFrequencies){

        return Math.sqrt(1);//byteSize d ???
    }
    
}
