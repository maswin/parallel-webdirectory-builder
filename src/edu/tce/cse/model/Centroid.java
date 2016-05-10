package edu.tce.cse.model;

import java.io.Serializable;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import edu.tce.cse.document.DocNode;

public class Centroid implements Serializable{
	public long clusterId;
	public DoubleMatrix1D tfIdf;
	public Centroid(long clusterId, DoubleMatrix1D tfIdf){
		this.clusterId = clusterId;
		this.tfIdf = tfIdf;
	}
	
	public float findCosSimilarity(Centroid that){

		DoubleMatrix1D vector1 = this.tfIdf;
        DoubleMatrix1D vector2 = that.tfIdf;

        DenseDoubleAlgebra algebra = new DenseDoubleAlgebra();

        float sim = (float) (vector1.zDotProduct(vector2) / 
                (algebra.norm2(vector1)*algebra.norm2(vector2)));
        if(Float.isNaN(sim)){
        	return 0f;
        }
        return sim;
	}
	
	public float findDistance(Centroid that) {
		return (1-findCosSimilarity(that));
	}
}
