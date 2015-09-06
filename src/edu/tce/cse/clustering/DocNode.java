package edu.tce.cse.clustering;
import java.util.HashMap;
import java.util.List;


public class DocNode extends Node implements Comparable<DocNode>{
	int signature[];
	float centrality;
	float sig;
	float delta;
	public float priority;
	int level;
	HashMap<Integer, List<Edge<DocNode>>> pred;
	//public FibonacciHeap.Node<DocNode> node;
	public DocNode(int id, int[] sig){
		super(id);
		signature = sig;
		priority = Float.MAX_VALUE;
		pred = new HashMap<Integer, List<Edge<DocNode>>>();
	}
	public float findCosSimilarity(DocNode d){
		double E = 0;
		for (int i = 0; i < d.signature.length; i++) {
			E += (this.signature[i] == d.signature[i] ? 1 : 0);
		}
		//return (float)E;
		E = E / signature.length;
		return (float)(Math.abs(E));
		//return (float)(Math.abs(Math.cos((1 - E) * Math.PI)));
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
