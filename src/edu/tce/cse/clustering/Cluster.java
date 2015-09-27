package edu.tce.cse.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import edu.tce.cse.document.DocNode;
import edu.tce.cse.util.KDTree;

public class Cluster extends Node implements Serializable{
	List<Node> nodes;
	List<DocNode> repPoints;
	float weightedDegreeInMST;
	public Cluster(long id){
		super(id);
		nodes = new ArrayList<Node>();
		repPoints = new ArrayList<DocNode>();
	}
	public void setNodeID(long id){
		nodeID = id;
	}
	public List<DocNode> getRepPoints() {
		return repPoints;
	}
	public void setRepPoints(List<DocNode> repPoints) {
		this.repPoints = repPoints;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	public float getDegreeInMST() {
		return weightedDegreeInMST;
	}

	public void setDegreeInMST(float degreeInMST) {
		this.weightedDegreeInMST = degreeInMST;
	}
	
	//to find representative points when documents are grouped to form initial cluster
	//fix number of repPoints & ratio of high centrality and low centrality points mix
	void findRepPointsBasedOnCentrality(List<DocNode> nodes){
		nodes.sort(new CentralityComparator());
		//this.repPoints.addAll(nodes);
		int size = nodes.size();
		
		int numOfRepPoints = (int)Math.ceil(size/2.0);
		int numOfHighCentrality = (int)Math.ceil(1*numOfRepPoints);
		int numOfLowCentrality= numOfRepPoints - numOfHighCentrality;
		repPoints.addAll(nodes.subList(size-numOfHighCentrality, size));
		if(numOfLowCentrality>0)
			repPoints.addAll(nodes.subList(0, numOfLowCentrality));
	}
	
	//to find representative points when clusters are grouped to form next level cluster
	void findRepPointsBasedOnMSTDegree(){
		//fix max ration of rep points to be picked
		float maxRatioOfRepPoints = 0.5f;
		nodes.sort(new DegreeComparator());
		Cluster c = ((Cluster)(nodes.get(nodes.size()-1)));
		//for node with maximum weighted degree, ratio of rep points picked = maxRatioOfRepPoints
		int numOfRepPoints = c.repPoints.size();
		//for node with maximum weighted degree, num of rep points picked = max
		int max = (int) Math.abs(Math.ceil(maxRatioOfRepPoints*numOfRepPoints));
		
		float proportion = max/c.weightedDegreeInMST;
		for(int i=0; i<nodes.size(); i++){
			c = ((Cluster)(nodes.get(i)));
			numOfRepPoints = (int) Math.abs(Math.ceil(proportion*c.weightedDegreeInMST));
			numOfRepPoints = Math.min(numOfRepPoints, c.repPoints.size());
			for(int j=0; j< numOfRepPoints; j++){
				repPoints.add(c.repPoints.get(j));
			}
		}
	}

	void checkCentralityHeuristic(List<DocNode> nodes){
		if(nodes.size()==1)
			return ;
		float max = Float.MIN_VALUE;
		float minCentrality = Float.MAX_VALUE; float maxCentrality = Float.MIN_VALUE;
		int minNode = -1; int maxNode = -1;
		int actual1 = -1; int actual2 = -1;
		for(int i=0; i<nodes.size(); i++){
			if(nodes.get(i).centrality<minCentrality){
				minCentrality = nodes.get(i).centrality;
				minNode = i;
			}
			if(nodes.get(i).centrality>maxCentrality){
				maxCentrality = nodes.get(i).centrality;
				maxNode = i;
			}
			for(int j= i+1; j<nodes.size(); j++){
				float dist = nodes.get(i).findEuclideanSimilarity(nodes.get(j));
				if(dist>max){
					max = dist;
					actual1 = i; 
					actual2 = j;
				}	
			}
		}
		float heuristicMax = nodes.get(minNode).findEuclideanSimilarity(nodes.get(maxNode));
		System.out.println(heuristicMax+", actual = "+max);
	}
	
	//to add the DocNode objects to a list and call findRepPoints()
	void formCluster(List<? extends Node> nodes){
		try{
			//merging DocNode objects to form an initial cluster
			if(nodes.get(0) instanceof DocNode){
				List<DocNode> list = (List<DocNode>)nodes;
				checkCentralityHeuristic(list);
				findRepPointsBasedOnCentrality(list);
				this.nodes.addAll(nodes);
			}
			//merging clusters to form a merged cluster
			else if(nodes.get(0) instanceof Cluster){
				this.nodes.addAll(nodes);
				//find rep points for cluster
				findRepPointsBasedOnMSTDegree();
			}
		}
		catch(Exception e){
			System.out.println("couldn't form an inital cluster");
		}
	}

	//to find similarity/distance between two clusters
	public float findDistance(Node n){
		Cluster c = (Cluster)n;
		float avgDistance = 0.0f;
		for(int i=0; i<repPoints.size(); i++){
			for(int j=0; j<c.getRepPoints().size(); j++){
				avgDistance+=(repPoints.get(i).findDistance(c.getRepPoints().get(j)));
			}
		}
		avgDistance/=(repPoints.size()*c.getRepPoints().size());
		return avgDistance;
	}
}

//to sort based on ascending order of centrality
class CentralityComparator implements Comparator{
	public int compare(Object i, Object j){
		if(((DocNode)i).centrality<((DocNode)j).centrality)
			return -1;
		else if(((DocNode)i).centrality==((DocNode)j).centrality)
			return 0;
		return 1;
	}
}
//to sort based on ascending order of degree in MST
class DegreeComparator implements Comparator{
	public int compare(Object i, Object j){
		if(((Cluster)i).weightedDegreeInMST<((Cluster)j).weightedDegreeInMST)
			return -1;
		else if(((Cluster)i).weightedDegreeInMST==((Cluster)j).weightedDegreeInMST)
			return 0;
		return 1;
	}
}