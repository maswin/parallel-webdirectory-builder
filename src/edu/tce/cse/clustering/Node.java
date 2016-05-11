package edu.tce.cse.clustering;

import java.io.Serializable;

public abstract class Node implements Serializable{
	public long nodeID;
	public Node(long id){
		nodeID = id;
	}
	abstract public float findDistance(Node n);
	
	public float findReducedDistance(Node n) {
		System.out.println("Error in finding reduced weight - over ride it");
		return 0f;
	}
}
