package edu.tce.cse.clustering;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DocNode extends Node implements Comparable<DocNode>{
	boolean[] signature;
	double[] tfIdf;
	float centrality;
	float sig;
	float delta;
	public float priority;
	int level;
	HashMap<Integer, List<Edge<DocNode>>> pred;
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
	public float findCosSimilarity(DocNode d){
		/*double E = 0;
		for (int i = 0; i < d.signature.length; i++) {
			E += (this.signature[i] == d.signature[i] ? 1 : 0);
		}
		//return (float)E;
		E = E / signature.length;
		return (float)(Math.abs(E));
		//return (float)(Math.abs(Math.cos((1 - E) * Math.PI)));*/
		double E = 0.0;
		double E1 = 0.0;
		double E2 = 0.0;
		for(int i=0;i<tfIdf.length;i++){
			E1 += Math.pow(this.tfIdf[i],2);
			E2 += Math.pow(d.tfIdf[i],2);
			E += this.tfIdf[i]*d.tfIdf[i];
		}
		E1 = Math.sqrt(E1);
		E2 = Math.sqrt(E2);
		E = (E / (E1*E2));
		return (float)(Math.abs(E));
	}
	@Override
	public float findEdgeWEight(Node n) {
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
