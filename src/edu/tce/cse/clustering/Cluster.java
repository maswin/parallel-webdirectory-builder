package edu.tce.cse.clustering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Cluster extends Node{
	List<Node> nodes;
	List<DocNode> repPoints;
	public Cluster(int id){
		super(id);
		nodes = new ArrayList<Node>();
		repPoints = new ArrayList<DocNode>();
	}
	void findRepPoints(List<DocNode> nodes){
		nodes.sort(new CentralityComparator());
		int size = nodes.size();
		int numOfRepPoints = (int)Math.ceil(size/2.0);
		int numOfHighCentrality = (int)Math.ceil(0.6*numOfRepPoints);
		int numOfLowCentrality= numOfRepPoints - numOfHighCentrality;
		repPoints.addAll(nodes.subList(size-numOfHighCentrality, size));
		if(numOfLowCentrality>0)
			repPoints.addAll(nodes.subList(0, numOfLowCentrality));
	}
	void formCluster(List<? extends Node> nodes){
		try{
			if(nodes.get(0) instanceof DocNode){
				List<DocNode> list = (List<DocNode>)nodes;
				findRepPoints(list);
				this.nodes.addAll(nodes);
			}
		}
		catch(Exception e){
			System.out.println("couldn't form an inital cluster");
		}
	}
	public float findEdgeWEight(Node n){
		Cluster c = (Cluster)n;
		return 0;
	}
}
class CentralityComparator implements Comparator{
	public int compare(Object i, Object j){
		if(((DocNode)i).centrality<((DocNode)j).centrality)
			return -1;
		else if(((DocNode)i).centrality==((DocNode)j).centrality)
			return 0;
		return 1;
	}
}