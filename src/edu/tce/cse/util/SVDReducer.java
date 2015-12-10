package edu.tce.cse.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import edu.tce.cse.document.DocMemManager;
import edu.tce.cse.document.Document;
import edu.tce.cse.example.sampleData;

public class SVDReducer {

	public int k = 25;

	public SVDReducer(){
		
	}
	public SVDReducer(int k){
		this.k = k;
	}
	public DoubleMatrix2D reduceTfIdf(DoubleMatrix2D tfIdfMatrix){
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
		return dCrossKSpace;
	}
	public DoubleMatrix2D reduceMatrix(int k, DoubleMatrix2D singularValues, DoubleMatrix2D rightSingularVector){
		
		DoubleMatrix2D singularValuesK = singularValues.viewPart(0,0,k,k);
		DoubleMatrix2D rightSingularVectorK = rightSingularVector.viewPart(0,0,rightSingularVector.rows(),k);
				
		DoubleMatrix2D dCrossKSpace = new DenseDoubleMatrix2D(rightSingularVectorK.rows(),k);
		rightSingularVectorK.zMult(singularValuesK,dCrossKSpace);
		
		return dCrossKSpace;
		
	}
	public void reduceDocTfIdf(List<Long> list){
		int tfIdfSize = DocMemManager.getDocument(0).getTfIdf().size();
		DoubleMatrix2D fullVector = new DenseDoubleMatrix2D(list.size(), tfIdfSize);
		//double[][] fullVector = new double[list.size()][];
		int index = 0;
		for(Long dId : list){
			Document doc = DocMemManager.getDocument(dId);
			double[] tfIdf = doc.getTfIdf().toArray();
			for(int i=0;i<tfIdf.length;i++){
				fullVector.setQuick(index, i, tfIdf[i]);
			}
			fullVector.trimToSize();
			index++;
		}
		fullVector = reduceTfIdf(fullVector);	
		index = 0;
		for(Long dId : list){
			double[] tfIdf = new double[tfIdfSize];
			Document doc = DocMemManager.getDocument(dId);
			for(int i=0;i<tfIdfSize;i++){
				tfIdf[i] = fullVector.getQuick(index, i);
			}
			doc.setTfIdf(new SparseDoubleMatrix1D(tfIdf));
			index++;
		}
		
	}
	

}
