package edu.tce.cse.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import edu.tce.cse.clustering.Document;

public class SVDTester {
	public static void main(String args[]) throws IOException{
		List<Document> list=new ArrayList<Document>();
		sampleData sd = new sampleData();
		list = sd.getSampleDoc();
		double[][] fullVector = new double[list.size()][];
		int index = 0;
		for(Document doc : list){
			fullVector[index] = doc.getTfIdf();
			index++;
		}
		DoubleMatrix2D fullMatrix = new DenseDoubleMatrix2D(fullVector);
		fullMatrix = fullMatrix.viewDice();
		SingularValueDecomposition s = new SingularValueDecomposition(fullMatrix);
		
		DoubleMatrix2D U = s.getU();
		DoubleMatrix2D Ss = s.getS();
		DoubleMatrix2D V = s.getV();
		
		//Ss = Ss.viewDice();
		fullVector = Ss.toArray();
		
		/*for(double[] val : fullVector){
			System.out.println(Arrays.toString(val));
		}
		System.out.println("\n1.Over\n");
		System.out.println(fullVector.length+" "+fullVector[0].length);*/
		
		/*fullVector = U.toArray();
		for(double[] val : fullVector){
			System.out.println(Arrays.toString(val));
		}
		System.out.println("\n2.Over\n");
		System.out.println(fullVector.length+" "+fullVector[0].length);*/
		
		fullVector = V.toArray();
		for(double[] val : fullVector){
			System.out.println(Arrays.toString(val));
		}
		System.out.println("\n3.Over\n");
		System.out.println(fullVector.length+" "+fullVector[0].length);
		

	}
}
