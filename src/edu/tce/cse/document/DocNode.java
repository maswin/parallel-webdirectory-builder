package edu.tce.cse.document;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import edu.tce.cse.clustering.Edge;
import edu.tce.cse.clustering.Node;


public class DocNode extends Node implements Comparable<DocNode>, Serializable{
	
	public String fileName;
	public boolean[] signature;
	public double[] tfIdf;
	public float centrality;
	public float sig;
	public float delta;
	public float priority;
	public int level;
	public HashMap<Integer, List<Edge<DocNode>>> pred;
	//public FibonacciHeap.Node<DocNode> node;
	public DocNode(long id, boolean[] sig, double[] tfIdf){
		super(id);
		this.signature = sig;
		this.tfIdf = tfIdf;
		priority = Float.MAX_VALUE;
		pred = new HashMap<Integer, List<Edge<DocNode>>>();
	}
	//Getter & Setter
	public boolean[] getSignature() {
		return signature;
	}
	public void setSignature(boolean[] signature) {
		this.signature = signature;
	}
	public double[] getTfIdf() {
		return tfIdf;
	}
	public void setTfIdf(double[] tfIdf) {
		this.tfIdf = tfIdf;
	}
	public float getCentrality(){
		return centrality;
	}
	public void setFileName(String name){
		this.fileName = name; 
	}
	public float findCosSimilarity(DocNode d){

		DoubleMatrix1D vector1 = new DenseDoubleMatrix1D(this.getTfIdf());
        DoubleMatrix1D vector2 = new DenseDoubleMatrix1D(d.getTfIdf());

        DenseDoubleAlgebra algebra = new DenseDoubleAlgebra();
        
        return (float) (vector1.zDotProduct(vector2) / 
                (algebra.norm2(vector1)*algebra.norm2(vector2)));
	}
	public float findCosDistance(DocNode d){
		return (1-findCosSimilarity(d));
	}
	public float findSignatureCosSimilarity(DocNode d){
		double E = 0;
		for (int i = 0; i < d.signature.length; i++) {
			E += (this.signature[i] == d.signature[i] ? 1 : 0);
		}
		E = E / signature.length;
		return (float)(Math.abs(E));
	}
	public float findEuclideanSimilarity(DocNode d){
		float E = 0.0f;
		for(int i=0; i<tfIdf.length; i++){
			E += Math.pow((tfIdf[i]-d.tfIdf[i]), 2);
		}
		return (float)(Math.abs(Math.sqrt(E)));
	}
	@Override
	public float findDistance(Node n) {
		// TODO Auto-generated method stub
		DocNode d = (DocNode)n;
		return findCosSimilarity(d);
	}
	@Override
	public int compareTo(DocNode o) {
		if (priority > o.priority) {
			return +1;
		} else {
			return -1;
		}
	}
}
