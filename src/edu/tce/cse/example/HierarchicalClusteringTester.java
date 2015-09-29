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
import edu.tce.cse.clustering.Node;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.model.Data;
import edu.tce.cse.model.Directory;
import edu.tce.cse.util.Statistics;

public class HierarchicalClusteringTester {
	Map<Long, Cluster> clustersAtThisLevel;
	DocNode[] localRepPoints;
	boolean[] flag = new boolean[1];
	int share[]=new int[2];
	public void mergeClusters(List<Data> list, int startID){
		List<Cluster> temp = new ArrayList<Cluster>();
		temp.addAll(clustersAtThisLevel.values());
		Graph graph = new Graph(temp);
		HashMap<Cluster, HashMap<Cluster, Float>> adjList=new HashMap(); 

		//use addEdge() to add each edge between cluster nodes. Update edge weight accordingly
		for(Data data: list){
			Cluster a = clustersAtThisLevel.get(data.a.clusterID);
			Cluster b = clustersAtThisLevel.get(data.b.clusterID);

			if(a.nodeID<b.nodeID){
				if(!adjList.containsKey(a))
					adjList.put(a, new HashMap());
				float val = 0f;
				if(adjList.get(a).containsKey(b))
					val = adjList.get(a).get(b);
				adjList.get(a).put(b, val+a.findDistance(b));
			}
			else if(a.nodeID>b.nodeID){
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
			if(edges!=null){
				for(Edge e: edges){
					if(!used.contains(e.getDst())){
						values[count++] = e.getWeight();
					}
					sum+=e.getWeight();
				}
			}
			c.setDegreeInMST(sum);
			used.add(c);
		}
		Statistics stats = new Statistics(values);
		float mean = stats.getMean();
		float stdDev = stats.getStdDev();
		//change threshold value here
		graph.removeInterClusterEdges(mean+(1.5f*stdDev), true);
		count = 1;
		List<List<DocNode>> components = graph.findConnectedComponents();

		int startingClusterID = (int) (startID); //keep track of last node ID that has been assigned 
		clustersAtThisLevel = graph.formClusters(components, startingClusterID);
	}
	public void distributeRepPoints(List<Cluster> clusters){
		Object[] repPoints = new Object[1];
		repPoints = getRepPoints(clusters);
		MPI.COMM_WORLD.Bcast(share, 1, 1, MPI.INT, 0);
		int numOfThreads = MPI.COMM_WORLD.Size();
		MPI.COMM_WORLD.Bcast(share, 0, 1, MPI.INT, 0);
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
				if(MPI.COMM_WORLD.Rank()==i)
					localRepPoints = new DocNode[sendcount[i]];
			}
		}
		//MPI.COMM_WORLD.Barrier();
		MPI.COMM_WORLD.Scatterv(repPoints, 0, sendcount, displs, MPI.OBJECT, localRepPoints, 0, localRepPoints.length, MPI.OBJECT, 0);

	}

	public Object[] getRepPoints(List<Cluster> clusters){
		List<DocNode> repPoints = new ArrayList<DocNode>();
		for(Cluster c: clusters){
			if(c==null)
				break;
			repPoints.addAll(c.getRepPoints());
		}
		share[1] = repPoints.size();

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
		int clustersInPreviousLevel = hc.clustersAtThisLevel.size();
		
		int startID = hc.clustersAtThisLevel.size();
		while(true){
			MPI.COMM_WORLD.Barrier();
			clustersInPreviousLevel = hc.clustersAtThisLevel.size();
			List<Cluster> temp = new ArrayList<Cluster>();
			temp.addAll(hc.clustersAtThisLevel.values());
			hc.distributeRepPoints(temp);
			temp.clear();
			MPI.COMM_WORLD.Barrier();
			if(MPI.COMM_WORLD.Rank()==0)
				System.out.println("\nRepresentative points are distributed");
			dLSH.hash(hc.localRepPoints);
			if(MPI.COMM_WORLD.Rank()==0)
				System.out.println("Representative points are hashed");

			if(MPI.COMM_WORLD.Rank()==0){
				List<Data> data = dLSH.getPairPoints();
				hc.mergeClusters(data, startID);
				startID+=hc.clustersAtThisLevel.size();
				System.out.println("--Merging of clusters--");
				System.out.println("\n \n");
				for(Cluster c: hc.clustersAtThisLevel.values()){
					System.out.println("\n Cluster "+c.nodeID+" - representative points:");
					for(DocNode d: c.getRepPoints()){
						d.setClusterID(c.nodeID);
						System.out.print(((DocNode)d).fileName+" ");

					}
					if(c.getChildren().size()>1){
						System.out.println("\n Children:");
						for(Node n: c.getChildren()){
							System.out.print(n.nodeID+" ");
						}
					}
				}
				//or check if clusters don't change between two levels?
				if(hc.clustersAtThisLevel.size()<=k || hc.clustersAtThisLevel.size()==clustersInPreviousLevel){
					hc.flag[0]=true;
				}
			}
			MPI.COMM_WORLD.Bcast(hc.flag, 0, 1, MPI.BOOLEAN, 0);
			if(hc.flag[0])
				break;

		}
		MPI.Finalize();
	}
}
