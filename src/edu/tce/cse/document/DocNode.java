package edu.tce.cse.document;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import edu.tce.cse.clustering.Edge;
import edu.tce.cse.clustering.Node;


public class DocNode extends Node implements Comparable<DocNode>{
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
		signature = sig;
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
	public float findCosSimilarity(DocNode d){
		DoubleMatrix1D vector1 = new DenseDoubleMatrix1D(this.tfIdf);
        DoubleMatrix1D vector2 = new DenseDoubleMatrix1D(d.tfIdf);

        Algebra algebra = new Algebra();
        
        return (float) (vector1.zDotProduct(vector2) / 
                (algebra.norm2(vector1)*algebra.norm2(vector2)));
	}
	public float findSignatureCosSimilarity(DocNode d){
		double E = 0;
		for (int i = 0; i < d.signature.length; i++) {
			E += (this.signature[i] == d.signature[i] ? 1 : 0);
		}
		E = E / signature.length;
		return (float)(Math.abs(E));
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
