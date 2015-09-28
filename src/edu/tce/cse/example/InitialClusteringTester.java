package edu.tce.cse.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.*;
import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Graph;
import edu.tce.cse.clustering.Node;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import edu.tce.cse.model.Directory;
import edu.tce.cse.util.Statistics;

public class InitialClusteringTester {

	public static void main(String args[])throws Exception{
		MPI.Init(args);
		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		Directory directory = new Directory();
		System.out.println("Started Id : "+id+"/"+size);
		InitialClusteringTester obj = new InitialClusteringTester();
		List<DocNode> nodeList = obj.preprocess();
		obj.performInitialClustering(nodeList, directory);
		MPI.Finalize();
	}
	public List<DocNode> preprocess(){
		List<Document> list=new ArrayList<Document>();
		List<DocNode> nodeList=new ArrayList<DocNode>();
		DocumentInitializer DI = new DocumentInitializer("TestDocuments");
		list = DI.getDocumentList();
		sampleData sd = new sampleData();
		//obj.list = sd.getSampleDoc();
		try {
			nodeList = sd.getSampleDocNodes(list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodeList;
	}
	public Map<Long, Cluster> performInitialClustering(List<DocNode> docs, Directory directory){
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
			graph.V.get(i).container = null;
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
		List<List<Node>> components = graph.findConnectedComponents();
		/*for(List<DocNode> x: components){
			System.out.println("Cluster "+count);
			for(DocNode d: x){
				System.out.print(fileNameMap.get(d.nodeID)+" ");
			}
			count++;
			System.out.println(" ");
		}*/
		List<Cluster> clusters= graph.formLeafClusters(components, 0, directory);
		MPI.COMM_WORLD.Barrier();

		return distributeInitialClusters(clusters.toArray());
	}

    public Map<Long, Cluster> distributeInitialClusters(Object[] localClusters){
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
		HashMap<Long, Cluster> clusterMap = new HashMap();
		if(MPI.COMM_WORLD.Rank()==0){
			//assign starting cluster id to index!!!
			long index = 0;	
			for(Cluster c: clusters){
				c.setNodeID(index);
				clusterMap.put(c.nodeID, c);
				index++;
				System.out.println("\n Node(cluster) "+c.nodeID+" - representative points:");
				for(DocNode d: c.getRepPoints()){
					System.out.print(((DocNode)d).fileName+" ");
					d.setClusterID(c.nodeID);
				}
			}
		}
		//ArrayList<Cluster> c = new ArrayList<>();
		MPI.COMM_WORLD.Barrier();
		System.out.println("Done!");
		
		return clusterMap;
	}
}
//combo 1: 0.3f-sparsification Math.min mean+stdDev
//combo 2: 0.4f-sparsification Math.max mean+1.5*stdDev
