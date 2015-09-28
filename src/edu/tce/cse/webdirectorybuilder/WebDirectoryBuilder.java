package edu.tce.cse.webdirectorybuilder;

import java.util.ArrayList;
import java.util.List;

import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Graph;
import edu.tce.cse.clustering.Node;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import edu.tce.cse.util.Statistics;
import mpi.MPI;

public class WebDirectoryBuilder {
	public static final String inputFolder = "TestDocuments";
	
	public List<Cluster> initialClustering(List<DocNode> nodeList){
		Graph<DocNode> graph = new Graph(nodeList);
		graph.addEdges();
		
		//modify sparsification exponent here
		graph.sparsify(0.3f);
		graph.findCentrality();
		
		
		float[] values = new float[graph.V.size()];
		System.out.println("");
		System.out.println("Betweenness Centrality values:");
		for(int i=0; i<graph.V.size(); i++){
			System.out.println(graph.V.get(i).nodeID+": "+graph.V.get(i).getCentrality());
			values[i] = graph.V.get(i).getCentrality();
		}
		
		
		Statistics stats = new Statistics(values);
		float mean = stats.getMean();
		float stdDev= stats.getStdDev();

		System.out.println("Vertices having centrality scores greater than or equal to mean: ");
		for(int i=0; i<graph.V.size(); i++){
			if(graph.V.get(i).getCentrality()>=mean)
				System.out.print(i+" ");
		}
		
		graph.removeInterClusterEdges(mean+(1f*stdDev), false);
		
		List<List<Node>> components = graph.findConnectedComponents();
		printComponent(components);
		
		List<Cluster> clusters = graph.formClusters(components, graph.V.size());
		printCluster(clusters);
		
		return clusters;
	}
	public static void main(String args[]){
		MPI.Init(args);
		
		//Generate Data - Set of Documents for each Processor
		DocumentInitializer DI = new DocumentInitializer(inputFolder);
		List<DocNode> docNodeList = DI.getDocNodeList();
		
		WebDirectoryBuilder WDB = new WebDirectoryBuilder();
		
		//Perform Initial Clustering
		List<Cluster> clusters = WDB.initialClustering(docNodeList);
		
		
		MPI.Finalize();
	}
	
	//Testing Purpose Prints
	public static void printComponent(List<List<DocNode>> components){
		MPI.COMM_WORLD.Barrier();
		
		for(int i=0;i<MPI.COMM_WORLD.Size();i++){
			MPI.COMM_WORLD.Barrier();
			if(i==MPI.COMM_WORLD.Rank()){
				System.out.println("Processor : "+MPI.COMM_WORLD.Rank());
				int count = 1;
				for(List<DocNode> x: components){
					System.out.println("Cluster "+count);
					for(DocNode d: x){
						System.out.print(d.fileName+" ");
					}
					count++;
					System.out.println(" ");
				}
				System.out.println(" ");
			}
		}
	}
	public static void printCluster(List<Cluster> clusters){
		MPI.COMM_WORLD.Barrier();
		
		for(int i=0;i<MPI.COMM_WORLD.Size();i++){
			MPI.COMM_WORLD.Barrier();
			if(i==MPI.COMM_WORLD.Rank()){
				System.out.println("Processor : "+MPI.COMM_WORLD.Rank());
				for(Cluster c: clusters){
					System.out.println("\n Node(cluster) "+c.nodeID+" - representative points:");
					for(DocNode d: c.getRepPoints()){
						System.out.print(d.fileName+" ");
					}
				}
				System.out.println(" ");
			}
		}
	}
}
