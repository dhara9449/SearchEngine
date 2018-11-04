package cecs429.TermFrequency;

public class WackyStrategy implements  TermFrequencyStrategy {
    public double calculateWqt(int N, int dft){

        return Math.max(0,Math.log( (N-dft)/dft));
    }
    public  double calculateWdt(String path,int tf){
        double nr = 1+Math.log(tf);
        double dr= 1+Math.log(tf);// not right - refer formula
        return nr/dr;
    }
    public  double calculateLd(String path,int docId){
        double byteSize = 0.0; // retrieve 4*docId*8 + 8*3 byte from the path
        //retreive  docId*8*4 + 3*8  from path
        return Math.sqrt(byteSize);
    }
    
}
