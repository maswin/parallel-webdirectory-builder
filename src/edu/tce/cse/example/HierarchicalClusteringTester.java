package edu.tce.cse.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mpi.*;
import edu.tce.cse.LSH.DistributedLSH;
import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Edge;
import edu.tce.cse.clustering.Graph;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.model.Data;
import edu.tce.cse.model.Directory;
import edu.tce.cse.util.Statistics;

public class HierarchicalClusteringTester {
	List<Cluster> clustersAtThisLevel;
	DocNode[] localRepPoints;
	boolean[] flag = new boolean[1];
	int share[]=new int[2];
	public List<Cluster> mergeClusters(List<Cluster> clusters, List<Data> list, long startID){
		Graph graph = new Graph(clusters);
		HashMap<Cluster, HashMap<Cluster, Float>> adjList=new HashMap(); 
		//use addEdge() to add each edge between cluster nodes. Update edge weight accordingly
		for(Data data: list){
			Cluster a = clusters.get((int)(data.a.clusterID-startID));
			Cluster b = clusters.get((int)(data.b.clusterID-startID));
			if(a.nodeID<b.nodeID){
				if(!adjList.containsKey(a))
					adjList.put(a, new HashMap());
				float val = 0f;
				if(adjList.get(a).containsKey(b))
					val = adjList.get(a).get(b);
				adjList.get(a).put(b, val+a.findDistance(b));
			}
			else{
				if(!adjList.containsKey(b))
					adjList.put(b, new HashMap());
				float val = 0f;
				if(adjList.get(b).containsKey(a))
					val = adjList.get(b).get(a);
				adjList.get(b).put(a, val+a.findDistance(b));
			}
		}
		for(Cluster c: adjList.keySet()){
			for(Cluster neighbour: adjList.get(c).keySet()){
				graph.addEdge(c, neighbour, adjList.get(c).get(neighbour));
			}
		}
		adjList.clear();
		
		Graph mst = graph.findMST();
		float[] values = new float[mst.V.size()-1]; int count = 0;
		Set<Cluster> used = new HashSet();
		for(int i=0; i<mst.V.size(); i++){
			Cluster c = (Cluster) mst.V.get(i);
			List<Edge> edges = (List<Edge>) mst.adjList.get(c);
			float sum = 0f;
			for(Edge e: edges){
				if(!used.contains(e.getDst())){
					values[count++] = e.getWeight();
				}
				sum+=e.getWeight();
			}
			c.setDegreeInMST(sum);
			used.add(c);
		}
		Statistics stats = new Statistics(values);
		float mean = stats.getMean();
		float stdDev = stats.getStdDev();
		//change threshold value here
		graph.removeInterClusterEdges(mean+(1f*stdDev), true);
		count = 1;
		List<List<DocNode>> components = graph.findConnectedComponents();
		
		int startingClusterID = (int) (startID + clusters.size()); //keep track of last node ID that has been assigned 
		clusters= graph.formClusters(components, startingClusterID);
		return clusters;
	}
	public void distributeRepPoints(List<Cluster> clusters){
		//int totalRepPoints = 0;
		//DocNode[] repPoints = new DocNode[totalRepPoints];
		//int index = 0;
		
		Object[] repPoints = new Object[1];
		if(MPI.COMM_WORLD.Rank()==0)
			{
				repPoints = getRepPoints(clusters);
				System.out.println(repPoints.length);
			}
		MPI.COMM_WORLD.Barrier();
		int numOfThreads = MPI.COMM_WORLD.Size();
		MPI.COMM_WORLD.Bcast(share, 0, 1, MPI.INT, 0);
		MPI.COMM_WORLD.Barrier();
		System.out.println(share[0]+" "+share[1]);
		localRepPoints = new DocNode[share[0]];
		int displs[] = new int[numOfThreads];
		int sendcount[] = new int[numOfThreads];
		for(int i=0; i<numOfThreads; i++){
			displs[i]=i*share[0];
			if(i!=numOfThreads-1){
				sendcount[i]=share[0];
			}
			else{
				sendcount[i]=(share[1]-(i*share[0]));
				localRepPoints = new DocNode[sendcount[i]];
			}
		}
		MPI.COMM_WORLD.Scatterv(repPoints, 0, sendcount, displs, MPI.OBJECT, localRepPoints, 0, localRepPoints.length, MPI.OBJECT, 0);
		MPI.COMM_WORLD.Barrier();
		
		
	}
	
	public Object[] getRepPoints(List<Cluster> clusters){
		List<DocNode> repPoints = new ArrayList<DocNode>();
		for(Cluster c: clusters){
			repPoints.addAll(c.getRepPoints());
		}
		share[1] = repPoints.size();
		MPI.COMM_WORLD.Bcast(share, 1, 1, MPI.INT, 0);
		Object[] store = repPoints.toArray();
		int numOfThreads = MPI.COMM_WORLD.Size();
		share[0] = (int)Math.ceil(store.length/numOfThreads);
		return store;
	}
	
	public static void main(String args[]){
		
		//fix threshold for number of clusters
		int k = 5; 
		//gather Clusters (initial) from all processors
		MPI.Init(args);
		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		Directory directory = new Directory();
		System.out.println("Started Id : "+id+"/"+size);
		InitialClusteringTester obj = new InitialClusteringTester();
		HierarchicalClusteringTester hc = new HierarchicalClusteringTester();
		DistributedLSH dLSH = new DistributedLSH();
		
		List<DocNode> nodeList = obj.preprocess();
		hc.clustersAtThisLevel = obj.performInitialClustering(nodeList, directory);
		
		while(true){
			MPI.COMM_WORLD.Barrier();
			hc.distributeRepPoints(hc.clustersAtThisLevel);
			MPI.COMM_WORLD.Barrier();
			if(MPI.COMM_WORLD.Rank()==0)
				System.out.println("Distributed");
			dLSH.hash(hc.localRepPoints);
			if(MPI.COMM_WORLD.Rank()==0)
				System.out.println("Hashed");
			
			if(MPI.COMM_WORLD.Rank()==0){
				List<Data> data = dLSH.getPairPoints();
				hc.clustersAtThisLevel = hc.mergeClusters(hc.clustersAtThisLevel, data, hc.clustersAtThisLevel.get(0).nodeID);
				System.out.println("Merged");
				for(Cluster c: hc.clustersAtThisLevel){
					System.out.println("\n Node(cluster) "+c.nodeID+" - representative points:");
					for(DocNode d: c.getRepPoints()){
						System.out.print(((DocNode)d).fileName+" ");
						d.setClusterID(c.nodeID);
					}
				}
				if(hc.clustersAtThisLevel.size()<=k){
					hc.flag[0]=true;
				}
			}
			MPI.COMM_WORLD.Bcast(hc.flag, 0, 1, MPI.BOOLEAN, 0);
			
			MPI.COMM_WORLD.Barrier();
			if(hc.flag[0])
				break;
		}
		MPI.Finalize();
	}
}

