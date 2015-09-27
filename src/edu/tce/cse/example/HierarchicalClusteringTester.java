package edu.tce.cse.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Edge;
import edu.tce.cse.clustering.Graph;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.util.KDTree;
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
}

