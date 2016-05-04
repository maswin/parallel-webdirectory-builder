package edu.tce.cse.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.model.Centroid;

public class Cluster extends Node implements Serializable{
	
    List<Node> children;
	List<DocNode> repPoints;
	Centroid centroid;
	float weightedDegreeInMST;
	public StringBuilder files;
	public double percent = 50.0;
	
	public Cluster(long id, List<? extends Node> nodes, double percent){
		super(id);
		children = new ArrayList<Node>();
		repPoints = new ArrayList<DocNode>();
		centroid = null;
		files = new StringBuilder();
		if(percent != 0.0)
			this.percent = percent;
		
		try{
			//merging DocNode objects to form an initial cluster
			if(nodes.get(0) instanceof DocNode){
				List<DocNode> list = (List<DocNode>)nodes;
				//checkCentralityHeuristic(list);
				findRepPointsBasedOnCentrality(list, this.percent);
				findInitialCentroid();
				addFiles(list);
			}
			//merging clusters to form a merged cluster
			else if(nodes.get(0) instanceof Cluster || nodes.get(0) instanceof LeafCluster){
				this.children.addAll(nodes);
				//find rep points for cluster
				//findRepPointsBasedOnMSTDegree();
				addAllRepresentativePoints();
				findCentroid();
				addFiles();
			}
		}
		catch(Exception e){
			System.out.println("couldn't form cluster");
		}
	}
	
	
	void addFiles(){
		Cluster c;
		for(int i=0; i<children.size(); i++){
			c = ((Cluster)(children.get(i)));
			this.files.append(";");
			this.files.append(c.files);
		}
	}
	void addFiles(List<DocNode> nodes){
		for(DocNode node : nodes){
			this.files.append(";");
			this.files.append(node.fileName);
		}
	}
	public void changeNodeID(long id){
		nodeID = id;
		//modify clusterID of centroid
		centroid.clusterId = id;
		//modify clusterID of representative points
		for(DocNode r: repPoints){
			r.setClusterID(id);
		}

	}
	public List<DocNode> getRepPoints() {
		return repPoints;
	}
	public void setRepPoints(List<DocNode> repPoints) {
		this.repPoints = repPoints;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> nodes) {
		this.children = nodes;
	}
	public float getDegreeInMST() {
		return weightedDegreeInMST;
	}

	public void setDegreeInMST(float degreeInMST) {
		this.weightedDegreeInMST = degreeInMST;
	}
	public Centroid getCentroid(){
		if(centroid == null){
			System.out.println("Centroid Not Found");
		}
		return this.centroid;
	}
	//to find representative points when documents are grouped to form initial cluster
	//fix number of repPoints & ratio of high centrality and low centrality points mix
		
	void findRepPointsBasedOnCentrality(List<DocNode> nodes, double percent){
	Collections.sort(nodes, new CentralityComparator());
		//nodes.sort(new CentralityComparator());
		int size = nodes.size();
		
		int numOfRepPoints = (int)Math.ceil(size*(percent/100.0));
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
		Collections.sort(children, new DegreeComparator());
		//children.sort(new DegreeComparator());
		Cluster c = ((Cluster)(children.get(children.size()-1)));
		//for node with maximum weighted degree, ratio of rep points picked = maxRatioOfRepPoints
		int numOfRepPoints = c.repPoints.size();
		//for node with maximum weighted degree, num of rep points picked = max
		int max = (int) Math.abs(Math.ceil(maxRatioOfRepPoints*numOfRepPoints));
		
		float proportion = max/c.weightedDegreeInMST;
		for(int i=0; i<children.size(); i++){
			c = ((Cluster)(children.get(i)));
			numOfRepPoints = (int) Math.abs(Math.ceil(proportion*c.weightedDegreeInMST));
			numOfRepPoints = Math.min(numOfRepPoints, c.repPoints.size());
			for(int j=0; j< numOfRepPoints; j++){
				c.repPoints.get(j).setClusterID(nodeID);
				repPoints.add(c.repPoints.get(j));			
			}
			
		}
		
	}

	void addAllRepresentativePoints(){
		Cluster c;
		for(int i=0; i<children.size(); i++){
			c = ((Cluster)(children.get(i)));
			for(int j=0;j<c.repPoints.size();j++){
				c.repPoints.get(j).setClusterID(nodeID);
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
	
	public Centroid findInitialCentroid(){
		double[][] vector = new double[this.repPoints.size()][];
        for (int i = 0; i < this.repPoints.size(); i++) {
            vector[i] = this.repPoints.get(i).getTfIdf().toArray();
        }

        DoubleMatrix2D matrix = new DenseDoubleMatrix2D(vector);

        double[] centroidTfIdf = new double[matrix.columns()];
        for (int col = 0; col < matrix.columns(); col++) {
            centroidTfIdf[col] = matrix.viewColumn(col).zSum();
        }
        
        Centroid centroid = new Centroid(this.nodeID, centroidTfIdf);
        this.centroid = centroid;
        return this.centroid;
	}
	
	public Centroid findCentroid(){
		double[][] vector = new double[this.children.size()][];

        for (int i = 0; i < this.children.size(); i++) {
        	Cluster c = (Cluster) this.children.get(i);
            vector[i] = c.getCentroid().tfIdf;
        }

        DoubleMatrix2D matrix = new DenseDoubleMatrix2D(vector);

        double[] centroidTfIdf = new double[matrix.columns()];
        for (int col = 0; col < matrix.columns(); col++) {
            centroidTfIdf[col] = matrix.viewColumn(col).zSum();
        }
        Centroid centroid = new Centroid(this.nodeID, centroidTfIdf);
        this.centroid = centroid;
        return this.centroid;
	}
	//Cluster Utility Functions
	public static double findMinClusterDiameter(List<Cluster> clusters){
		double minDist = Double.MAX_VALUE;		
		for(Cluster c : clusters){
			Collections.sort(c.children, new CentralityComparator());
			//c.children.sort(new CentralityComparator());
			DocNode minCentralityPoint = (DocNode) c.children.get(0);
			DocNode maxCentralityPoint = (DocNode) c.children.get(c.children.size());
			if(!minCentralityPoint.equals(maxCentralityPoint)){
				double dist = minCentralityPoint.findEuclideanSimilarity(maxCentralityPoint);
				if(dist<minDist){
					minDist = dist;
				}
			}
		}
		return minDist;
	}
	
	public static double findMinInterClusterDistance(List<Cluster> clusters){
		double minDist = Double.MAX_VALUE;
		List<DocNode> highCentralityPoints = new ArrayList<>(clusters.size());
		for(Cluster c : clusters){
			Collections.sort(c.repPoints, new CentralityComparator());
			//c.repPoints.sort(new CentralityComparator());
			DocNode maxCentralityPoint = (DocNode) c.repPoints.get(c.repPoints.size()-1);
			highCentralityPoints.add(maxCentralityPoint);
		}
		for(int i=0;i<highCentralityPoints.size();i++){
			for(int j=i+1;j<highCentralityPoints.size();j++){
				double dist = highCentralityPoints.get(i).
						findEuclideanSimilarity(highCentralityPoints.get(j));
				
				if(dist<minDist){
					minDist = dist;
				}
			}
		}
		return minDist;
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