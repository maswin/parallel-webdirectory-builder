package edu.tce.cse.model;

import java.util.HashMap;
import java.util.List;

import edu.tce.cse.clustering.Edge;
import edu.tce.cse.document.DocNode;

public class PartialBetweenness {
	public float sig;
	public float delta;
	public float priority;
	public int level;
	public HashMap<Integer, List<Edge<DocNode>>> pred;
	public PartialBetweenness(){
		priority = Float.MAX_VALUE;
		pred = new HashMap<Integer, List<Edge<DocNode>>>();
	}
}
