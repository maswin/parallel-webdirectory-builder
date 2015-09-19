package edu.tce.cse.example;

import java.util.ArrayList;
import java.util.List;
import mpi.*;

import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.DocNode;
import edu.tce.cse.clustering.Document;
import edu.tce.cse.clustering.Graph;
import edu.tce.cse.util.Statistics;

public class InitialClusteringTester {
	public static void main(String args[])throws Exception{
		List<Document> list=new ArrayList<Document>();
		List<DocNode> nodeList=new ArrayList<DocNode>();
		
		sampleData sd = new sampleData();
		list = sd.getSampleDoc();
		nodeList = sd.getSampleDocNodes(list);
		Graph<DocNode> graph = new Graph(nodeList);
		graph.addEdges();
		//modify sparsification exponent here
		graph.sparsify(0.3f);
		graph.findCentrality();
		
		float[] values = new float[graph.V.size()];
		//System.out.println("");
		//System.out.println("Betweenness Centrality values:");
		for(int i=0; i<graph.V.size(); i++){
			//System.out.println(graph.V.get(i).nodeID+": "+graph.V.get(i).getCentrality());
			values[i] = graph.V.get(i).getCentrality();
		}
		Statistics stats = new Statistics(values);
		float mean = stats.getMean();
		float stdDev= stats.getStdDev();
		/*System.out.println("Vertices having centrality scores greater than or equal to mean: ");
		for(int i=0; i<graph.V.size(); i++){
			if(graph.V.get(i).getCentrality()>=mean)
				System.out.print(i+" ");
		}*/
		graph.removeInterClusterEdges(mean+(1f*stdDev), false);
		int count = 1;
		List<List<DocNode>> components = graph.findConnectedComponents();
		for(List<DocNode> x: components){
			System.out.println("Cluster "+count);
			for(DocNode d: x){
				System.out.print(list.get((int)d.nodeID).getFilePath().substring(39)+" ");
			}
			count++;
			System.out.println(" ");
		}
		List<Cluster> clusters= graph.formClusters(components, graph.V.size());
		for(Cluster c: clusters){
			System.out.println("\n Node(cluster) "+c.nodeID+" - representative points:");
			for(DocNode d: c.getRepPoints()){
				System.out.print(list.get((int)d.nodeID).getFilePath().substring(39)+" ");
			}
		}
		
	}
}
//combo 1: 0.3f-sparsification Math.min mean+stdDev
//combo 2: 0.4f-sparsification Math.max mean+1.5*stdDev
