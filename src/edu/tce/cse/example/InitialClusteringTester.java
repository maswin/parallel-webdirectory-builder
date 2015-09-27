package edu.tce.cse.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mpi.*;
import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Graph;
import edu.tce.cse.clustering.Node;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import edu.tce.cse.util.Statistics;

public class InitialClusteringTester {

	public static void main(String args[])throws Exception{
		MPI.Init(args);
		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		System.out.println("Started Id : "+id+"/"+size);
		InitialClusteringTester obj = new InitialClusteringTester();
		List<Document> list=new ArrayList<Document>();
		List<DocNode> nodeList=new ArrayList<DocNode>();
		DocumentInitializer DI = new DocumentInitializer("/home/mukuntha/Downloads/TestDocuments");
		list = DI.getDocumentList();
		sampleData sd = new sampleData();
		//obj.list = sd.getSampleDoc();
		nodeList = sd.getSampleDocNodes(list);
		obj.performInitialClustering(nodeList);
		MPI.Finalize();
	}
	public void performInitialClustering(List<DocNode> docs){
		Graph<DocNode> graph = new Graph(docs);
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
		/*for(List<DocNode> x: components){
			System.out.println("Cluster "+count);
			for(DocNode d: x){
				System.out.print(fileNameMap.get(d.nodeID)+" ");
			}
			count++;
			System.out.println(" ");
		}*/
		List<Cluster> clusters= graph.formClusters(components, graph.V.size());
		MPI.COMM_WORLD.Barrier();
		
		distributeInitialClusters(clusters.toArray());
	}
	
	void distributeInitialClusters(Object[] localClusters){
		int numOfClusters[]=new int[MPI.COMM_WORLD.Size()];
		int displs[]=new int[MPI.COMM_WORLD.Size()];
		int numOfLocalClusters[]=new int[1];
		numOfLocalClusters[0]=localClusters.length;
		MPI.COMM_WORLD.Gather(numOfLocalClusters, 0, 1, MPI.INT, numOfClusters, 0, 1, MPI.INT, 0);
		int totalNumOfClusters = 0;
		for(int i=0; i<numOfClusters.length; i++){
			displs[i]=totalNumOfClusters;
			totalNumOfClusters+=numOfClusters[i];
		}
		Cluster[] clusters = new Cluster[totalNumOfClusters];
		MPI.COMM_WORLD.Gatherv(localClusters, 0, localClusters.length, MPI.OBJECT, clusters, 0, numOfClusters, displs, MPI.OBJECT, 0);
		if(MPI.COMM_WORLD.Rank()==0){
			//assign starting cluster id to index!!!
			long index = 0;	
			for(Cluster c: clusters){
					c.setNodeID(index);
					index++;
					System.out.print("\n"+c.nodeID+":  ");
					for(Node d: c.getNodes()){
						System.out.print(((DocNode)d).fileName+" ");
					}
					System.out.println("\n Node(cluster) "+c.nodeID+" - representative points:");
					for(DocNode d: c.getRepPoints()){
						System.out.print(((DocNode)d).fileName+" ");
					}
				}
		}
	}
}
//combo 1: 0.3f-sparsification Math.min mean+stdDev
//combo 2: 0.4f-sparsification Math.max mean+1.5*stdDev
