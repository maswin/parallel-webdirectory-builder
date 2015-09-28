package edu.tce.cse.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import edu.tce.cse.document.Document;
import edu.tce.cse.example.sampleData;

public class SVDReducer {

	public int k = 30;

	public double[][] reduceTfIdf(double[][] tfIdf){
		DoubleMatrix2D tfIdfMatrix = new DenseDoubleMatrix2D(tfIdf);
		tfIdfMatrix = tfIdfMatrix.viewDice();
		SingularValueDecomposition svd = new SingularValueDecomposition(tfIdfMatrix);
		
		DoubleMatrix2D leftSingularVector = svd.getU();
		DoubleMatrix2D singularValues = svd.getS();
		DoubleMatrix2D rightSingularVector = svd.getV();
		
		//System.out.println(leftSingularVector.rows()+" "+leftSingularVector.columns());
		//System.out.println(singularValues.rows()+" "+singularValues.columns());
		//System.out.println(rightSingularVector.rows()+" "+rightSingularVector.columns());
		DoubleMatrix2D dCrossKSpace = reduceMatrix(k, singularValues, rightSingularVector);
		//System.out.println(dCrossKSpace.rows()+" "+dCrossKSpace.columns());
		//System.out.println(svd.rank());
		return dCrossKSpace.toArray();
	}
	public DoubleMatrix2D reduceMatrix(int k, DoubleMatrix2D singularValues, DoubleMatrix2D rightSingularVector){
		
		DoubleMatrix2D singularValuesK = singularValues.viewPart(0,0,k,k);
		DoubleMatrix2D rightSingularVectorK = rightSingularVector.viewPart(0,0,rightSingularVector.rows(),k);
				
		DoubleMatrix2D dCrossKSpace = new DenseDoubleMatrix2D(rightSingularVectorK.rows(),k);
		rightSingularVectorK.zMult(singularValuesK,dCrossKSpace);
		
		return dCrossKSpace;
		
	}
	public void reduceDocTfIdf(List<Document> list){
		double[][] fullVector = new double[list.size()][];
		int index = 0;
		for(Document doc : list){
			fullVector[index] = doc.getTfIdf();
			index++;
		}
		fullVector = reduceTfIdf(fullVector);	
		index = 0;
		for(Document doc : list){
			doc.setTfIdf(fullVector[index]);
			index++;
		}
		
	}
	

}
