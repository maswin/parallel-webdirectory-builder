package cure;
import java.util.StringTokenizer;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import edu.tce.cse.document.DocNode;

/**
 * Represents a Point Class. Also stores the KD Tree index for search.
 * @version 1.0
 * @author jmishra
 */
public class Point {
	public DoubleMatrix1D tfIdf;
	public int index;
	public String fileName;
	
	public Point() {
		this.fileName = "Temp File";	
	}
	
	public Point(DoubleMatrix1D tfIdf, int index, String fileName) {
		this.tfIdf = tfIdf;
		this.index = index;
		this.fileName = fileName;
	}
	
	/*public double[] copyTfIdf(double[] tfIdf){
		/*double[] tmpTfIdf = new double[tfIdf.length];
		for(int i=0;i<tfIdf.length;i++){
			tmpTfIdf[i] = tfIdf[i];
		}
		return tmpTfIdf;
		return tfIdf;
	}*/
	public Point(Point point) {
		
		this.tfIdf = point.tfIdf;
		this.fileName = "Temp File";
		//Source code didn't do this
		//this.index = point.index;
	}
	
	public double[] toDouble() {
		return this.tfIdf.toArray(); 
	}
	
	public static Point parseString(String str) {
		Point point = new Point();
		StringTokenizer st = new StringTokenizer(str);
		return point;
	}
	
	public float findCosSimilarity(Point t){

		DoubleMatrix1D vector1 = this.tfIdf;
        DoubleMatrix1D vector2 = t.tfIdf;

        DenseDoubleAlgebra algebra = new DenseDoubleAlgebra();
        
        float sim = (float) (vector1.zDotProduct(vector2) / 
                (algebra.norm2(vector1)*algebra.norm2(vector2)));
        if(Float.isNaN(sim)){
        	return 0;
        }
        return sim;
        
	}
	/**
	 * Calculates the Euclidean Distance from a Point t
	 * @param t Point t
	 * @return
	 * double Euclidean Distance from a Point t
	 */
	public double calcDistanceFromPoint(Point t) {
		return (1-findCosSimilarity(t));
	}
	
	public String toString() {
		return fileName;
	}
	
	public boolean equals(Point t) {	
		return this.tfIdf.equals(t.tfIdf);
	}
}