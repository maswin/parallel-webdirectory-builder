package edu.tce.cse.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mpi.*;
import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Edge;
import edu.tce.cse.clustering.Graph;
import edu.tce.cse.clustering.KDTree;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.util.Statistics;

public class HierarchicalClusteringTester {

	public List<Cluster> mergeClusters(List<Cluster> clusters, List<KDTree> trees, HashMap<DocNode, Integer> nodeToTreeMap, int maxTreeHeight){
		Graph graph = new Graph(clusters);
		graph.addEdges(trees, nodeToTreeMap, maxTreeHeight);
		Graph mst = graph.findMST();
		float[] values = new float[mst.V.size()-1]; int count = 0;
		Set<Cluster> used = new HashSet();
		for(int i=0; i<mst.V.size(); i++){
			Cluster c = (Cluster) mst.V.get(i);
			List<Edge> edges = (List<Edge>) mst.adjList.get(c);
			c.setDegreeInMST(edges.size());
			for(Edge e: edges){
				if(!used.contains(e.getDst())){
					values[count++] = e.getWeight();
				}
			}
			used.add(c);
		}
		Statistics stats = new Statistics(values);
		float mean = stats.getMean();
		float stdDev = stats.getStdDev();
		graph.removeInterClusterEdges(mean+(1f*stdDev), true);
		count = 1;
		List<List<DocNode>> components = graph.findConnectedComponents();
		int startingClusterID = 0; //keep track of last node ID that has been assigned 
		clusters= graph.formClusters(components, startingClusterID);
		return clusters;
	}
	public void distributeRepPoints(Object[] repPoints){
		//int totalRepPoints = 0;
		//DocNode[] repPoints = new DocNode[totalRepPoints];
		//int index = 0;
		int numOfThreads = MPI.COMM_WORLD.Size();
		int share = (int)Math.ceil(repPoints.length/numOfThreads);
		DocNode[] localRepPoints = new DocNode[share];
		int displs[] = new int[numOfThreads];
		int sendcount[] = new int[numOfThreads];
		for(int i=0; i<numOfThreads; i++){
			displs[i]=i*share;
			if(i!=numOfThreads-1){
				sendcount[i]=share;
			}
			else{
				sendcount[i]=(repPoints.length-(i*share));
				localRepPoints = new DocNode[sendcount[i]];
			}
		}
		MPI.COMM_WORLD.Scatterv(repPoints, 0, sendcount, displs, MPI.OBJECT, localRepPoints, 0, localRepPoints.length, MPI.OBJECT, 0);
	}
	
	public Object[] getRepPoints(List<Cluster> clusters){
		List<DocNode> repPoints = new ArrayList<DocNode>();
		for(Cluster c: clusters){
			repPoints.addAll(c.getRepPoints());
		}
		Object[] store = repPoints.toArray();
		return store;
	}
	
	public void performHierarchicalClustering(){
		
		//fix threshold for number of clusters
		int k; 
		//gather Clusters (initial) from all processors
		List<Cluster> clustersAtThisLevel;
		
		
		while(true){
			//gather disjoint sets
			//form KD trees + nodeToTree map + get max tree height?
			//List<Cluster> nextLevelClusters = mergeClusters(clustersAtThisLevel, trees, nodeToTreeMap, maxTreeHeight);
			
		}
	}
}

