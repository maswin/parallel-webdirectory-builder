package edu.tce.cse.clustering;

public abstract class Node {
	public long nodeID;
	public Node(long id){
		nodeID = id;
	}
	abstract public float findEdgeWEight(Node n);
}
