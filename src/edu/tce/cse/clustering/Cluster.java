package edu.tce.cse.clustering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import edu.tce.cse.document.DocNode;
import edu.tce.cse.util.KDTree;

public class Cluster extends Node{
	List<Node> nodes;
	List<DocNode> repPoints;
	public Cluster(int id){
		super(id);
		nodes = new ArrayList<Node>();
		setRepPoints(new ArrayList<DocNode>());
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

	
	//to find representative points:fix number of repPoints & ratio of high centrality and low centrality points mix
	void findRepPointsBasedOnCentrality(List<DocNode> nodes){
		nodes.sort(new CentralityComparator());
		this.repPoints.addAll(nodes);
		int size = nodes.size();
		/*int numOfRepPoints = (int)Math.ceil(size/2.0);
		int numOfHighCentrality = (int)Math.ceil(1*numOfRepPoints);
		int numOfLowCentrality= numOfRepPoints - numOfHighCentrality;
		this.getRepPoints().addAll(nodes.subList(size-numOfHighCentrality, size));
		if(numOfLowCentrality>0)
			getRepPoints().addAll(nodes.subList(0, numOfLowCentrality));
		*/
	}

	//to add the DocNode objects to a list and call findRepPoints()
	void formCluster(List<? extends Node> nodes){
		try{
			//merging DocNode objects to form an intial cluster
			if(nodes.get(0) instanceof DocNode){
				List<DocNode> list = (List<DocNode>)nodes;
				findRepPointsBasedOnCentrality(list);
				this.nodes.addAll(nodes);
			}
			//merging clusters to form a merged cluster
			else if(nodes.get(0) instanceof Cluster){
				this.nodes.addAll(nodes);
				//find rep points for cluster (close to mean, far from mean)
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