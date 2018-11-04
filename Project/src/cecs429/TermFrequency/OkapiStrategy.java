package cecs429.TermFrequency;

import java.util.List;

public class OkapiStrategy implements  TermFrequencyStrategy{
    public double calculateWqt(int N, int dft){
        double dr= dft+0.5;
        double nr=N - dft+ 0.5;
        return Math.max(0.1, Math.log(nr/dr));
    }
    public  double calculateWdt(String path,int tf,int docId){
        double docLength_d= 1.0;//docId*8*4 + 8*2 from path
        double docLength_A = 1.0;//last 8 bytes in the filePath
        double nr= 2.2*tf;
        double dr= 1.2*(0.25+0.75*(docLength_d/docLength_A))+tf;
        return nr/dr;
    }
    public  double calculateLd(String path,int docId){
        return 1.0;
    }
    
}
