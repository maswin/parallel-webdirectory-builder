package edu.tce.cse.clustering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Cluster extends Node{
	List<Node> nodes;
	List<DocNode> repPoints;
	int degreeInMST;
	public Cluster(int id){
		super(id);
		nodes = new ArrayList<Node>();
		repPoints = new ArrayList<DocNode>();
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
	public int getDegreeInMST() {
		return degreeInMST;
	}

	public void setDegreeInMST(int degreeInMST) {
		this.degreeInMST = degreeInMST;
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
	//if 4 clusters are merged: x, 2x, 3x, 4x representative points are picked from the 4 clusters
	// 							sorted in ascending order of their degree in MST
	void findRepPointsBasedOnMSTDegree(){
		nodes.sort(new DegreeComparator());
		float sum = (nodes.size()*(nodes.size()+1))/2;
		int totalRepPoints = 0;
		for(int i=0; i<nodes.size(); i++){
			totalRepPoints+=((Cluster)nodes.get(i)).repPoints.size();
		}
		//fix number of rep points to be picked
		int numOfRepPoints = (int)Math.ceil(totalRepPoints/2.0);
		int x = (int) Math.abs(numOfRepPoints/sum);
		for(int i=0; i<nodes.size(); i++){
			for(int j=0; j<(i+1)*x; j++){
				repPoints.add(((Cluster)nodes.get(i)).repPoints.get(j));
			}
		}
	}

	//to add the DocNode objects to a list and call findRepPoints()
	void formCluster(List<? extends Node> nodes){
		try{
			//merging DocNode objects to form an initial cluster
			if(nodes.get(0) instanceof DocNode){
				List<DocNode> list = (List<DocNode>)nodes;
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

	//to find distance using KDTree LCA measure
	public float findDistanceUsingKDTreeMeasure(Cluster c, List<KDTree> trees, HashMap<DocNode, Integer> nodeToTreeMap, int maxTreeHeight){
		float avgDistance = 0.0f;
		for(int i=0; i<repPoints.size(); i++){
			for(int j=0; j<c.getRepPoints().size(); j++){
				DocNode node1 = repPoints.get(i);
				DocNode node2 = c.getRepPoints().get(j);
				if(nodeToTreeMap.get(node1)==nodeToTreeMap.get(node2)){
					avgDistance+=(trees.get(nodeToTreeMap.get(node1)).findDistance(node1, node2));
				}
				else{
					//fix penalty weight when rep points don't fall under same KD tree
					avgDistance+=(4*maxTreeHeight);
				}
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
		if(((Cluster)i).degreeInMST<((Cluster)j).degreeInMST)
			return -1;
		else if(((Cluster)i).degreeInMST==((Cluster)j).degreeInMST)
			return 0;
		return 1;
	}
}