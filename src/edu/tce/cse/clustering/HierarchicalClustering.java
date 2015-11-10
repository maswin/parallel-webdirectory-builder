package edu.tce.cse.clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
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
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import edu.tce.cse.example.sampleData;
import edu.tce.cse.model.Centroid;
import edu.tce.cse.model.Data;
import edu.tce.cse.model.Directory;
import edu.tce.cse.model.EdgeData;
import edu.tce.cse.util.Statistics;
import gui.TreeView;

public class HierarchicalClustering {
	public Map<Long, Cluster> clustersAtThisLevel;
	public DocNode[] localRepPoints;
	public Centroid[] centroids;
	public boolean[] flagToStop = new boolean[1];
	int shareDetails[]=new int[2];
	public double R;

	//to merge clusters to form the next level clusters, when given the results of LSH as input 
	public void mergeClusters(List<Data> list, int startID){
		List<Cluster> temp = new ArrayList<Cluster>();
		HashMap<Cluster, HashMap<Cluster, Float>> adjList=new HashMap(); 
		temp.addAll(clustersAtThisLevel.values());
		//form graph where each node is a cluster
		Graph graph = new Graph(temp);
		//use addEdge() to add each edge between cluster nodes. Update edge weight accordingly
		for(Data data: list){
			Cluster a = clustersAtThisLevel.get(data.a);
			Cluster b = clustersAtThisLevel.get(data.b);
			if(a.nodeID<b.nodeID){
				if(!adjList.containsKey(a))
					adjList.put(a, new HashMap());
				float weight;
				if(!adjList.get(a).containsKey(b)){
					weight = a.findDistance(b);
					adjList.get(a).put(b, weight);
				}

			}
			else if(a.nodeID>b.nodeID){
				if(!adjList.containsKey(b))
					adjList.put(b, new HashMap());
				float weight;
				if(!adjList.get(b).containsKey(a)){
					weight = a.findDistance(b);
					adjList.get(b).put(a, weight);
				}
			}
		}
		for(Cluster c: adjList.keySet()){
			temp.remove(c);
			for(Cluster neighbour: adjList.get(c).keySet()){
				temp.remove(neighbour);
				graph.addEdge(c, neighbour, adjList.get(c).get(neighbour));
				//System.out.print(c.nodeID+","+neighbour.nodeID+"("+adjList.get(c).get(neighbour)+") ");
			}
		}
		adjList.clear();

		//FIX THRESHOLD 
		Statistics stats; float mean=0f, stdDev=0f;
		if(temp.isEmpty()){ //the graph is connected
			Graph mst = graph.findMST(graph.V);
			float[] values = getMSTEdgeWeights(mst);
			stats = new Statistics(values);
			mean = stats.getMean();
			stdDev = stats.getStdDev();
			graph.removeInterClusterEdges(graph.V, mean+(1f*stdDev), true);
		}
		else{
			List<List<Node>> components = graph.findConnectedComponents();
			int count = 0;
			mean = 0;
			stdDev = 0;
			for(List<Node> component: components){
				if(component.size()>2){				
					component.forEach(n -> System.out.print(n.nodeID+" "));
					Graph mst = graph.findMST(component);
					float[] values = getMSTEdgeWeights(mst);
					stats = new Statistics(values);
					mean += stats.getMean();
					stdDev += stats.getStdDev();	
					count ++;
				}
			}
			if(count>0){
				mean = (float) (mean/(count*1.0));
				stdDev = (float) (stdDev/(count*1.0));
			}

			for(List<Node> component: components){
				if(component.size()>2)
					graph.removeInterClusterEdges(component, mean+(1f*stdDev), true);
			}
		}

		//change threshold value here
		//graph.removeInterClusterEdges(mean+(1f*stdDev), true);
		List<List<DocNode>> components = graph.findConnectedComponents();
		clustersAtThisLevel = graph.formClusters(components, startID);

	}

	//to return edge weights of MST 
	public float[] getMSTEdgeWeights(Graph mst){
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
					//System.out.print(e.getSrc().nodeID+","+e.getDst().nodeID+"("+e.getWeight()+") ");
				}
			}
			c.setDegreeInMST(sum);
			used.add(c);
		}
		return values;
	}

	//to distribute representative points from main processor to other processors
	public void distributeRepPoints(List<Cluster> clusters){
		Object[] repPoints = getRepPoints(clusters);
		MPI.COMM_WORLD.Bcast(shareDetails, 1, 1, MPI.INT, 0);
		int numOfThreads = MPI.COMM_WORLD.Size();
		MPI.COMM_WORLD.Bcast(shareDetails, 0, 1, MPI.INT, 0);
		localRepPoints = new DocNode[shareDetails[0]];
		int displs[] = new int[numOfThreads];
		int sendcount[] = new int[numOfThreads];
		for(int i=0; i<numOfThreads; i++){
			displs[i]=i*shareDetails[0];
			if(i!=numOfThreads-1){
				sendcount[i]=shareDetails[0];
			}
			else{
				sendcount[i]=(shareDetails[1]-(i*shareDetails[0]));
				if(MPI.COMM_WORLD.Rank()==i)
					localRepPoints = new DocNode[sendcount[i]];
			}
		}
		MPI.COMM_WORLD.Scatterv(repPoints, 0, sendcount, displs, MPI.OBJECT, localRepPoints, 0, localRepPoints.length, MPI.OBJECT, 0);

	}

	public void distributeCentroids(List<Cluster> clusters){
		Object[] centroidPoints = getCentroidPoints(clusters);
		MPI.COMM_WORLD.Bcast(shareDetails, 1, 1, MPI.INT, 0);
		int numOfThreads = MPI.COMM_WORLD.Size();
		MPI.COMM_WORLD.Bcast(shareDetails, 0, 1, MPI.INT, 0);
		centroids = new Centroid[shareDetails[0]];
		int displs[] = new int[numOfThreads];
		int sendcount[] = new int[numOfThreads];
		for(int i=0; i<numOfThreads; i++){
			displs[i]=i*shareDetails[0];
			if(i!=numOfThreads-1){
				sendcount[i]=shareDetails[0];
			}
			else{
				sendcount[i]=(shareDetails[1]-(i*shareDetails[0]));
				if(MPI.COMM_WORLD.Rank()==i)
					centroids = new Centroid[sendcount[i]];
			}
		}
		MPI.COMM_WORLD.Scatterv(centroidPoints, 0, sendcount, displs, MPI.OBJECT, centroids, 0, centroids.length, MPI.OBJECT, 0);
	}
	//to return all the representative points
	public Object[] getRepPoints(List<Cluster> clusters){
		List<DocNode> repPoints = new ArrayList<DocNode>();
		for(Cluster c: clusters){
			if(c==null)
				break;
			repPoints.addAll(c.getRepPoints());
		}
		Object[] store = repPoints.toArray();
		int numOfThreads = MPI.COMM_WORLD.Size();
		shareDetails[0] = (int)Math.ceil(store.length/numOfThreads);
		shareDetails[1] = repPoints.size();
		return store;
	}

	//to return all the centroid points
	public Object[] getCentroidPoints(List<Cluster> clusters){
		List<Centroid> centroidPoints = new ArrayList<Centroid>();
		for(Cluster c: clusters){
			if(c==null)
				break;
			centroidPoints.add(c.getCentroid());
		}
		Object[] store = centroidPoints.toArray();
		int numOfThreads = MPI.COMM_WORLD.Size();
		shareDetails[0] = (int)Math.ceil(store.length/numOfThreads);
		shareDetails[1] = centroidPoints.size();
		return store;
	}

	//preprocessing steps
	public List<DocNode> preprocess(){
		List<DocNode> nodeList=new ArrayList<DocNode>();
		DocumentInitializer DI = new DocumentInitializer("TestDocuments");
		nodeList = DI.getDocNodeList();
		return nodeList;
	}

	//to form initial clusters in each processor
	public Map<Long, Cluster> initialClustering(List<DocNode> docs, Directory directory){
		//form graph where each node is a DocNode
		Graph<DocNode> graph = new Graph(docs);
		graph.addEdges();
		//FIX SPARSIFICATION EXPONENT HERE
		graph.sparsify(0.3f);
		graph.findCentrality();

		float[] values = new float[graph.V.size()];
		for(int i=0; i<graph.V.size(); i++){
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
		//FIX THRESHOLD VALUE HERE
		graph.removeInterClusterEdges(graph.V, mean+(1f*stdDev), false);
		int count = 1;
		List<List<Node>> components = graph.findConnectedComponents();
		List<Cluster> clusters= graph.formLeafClusters(components, 0, directory);
		for(int i=0; i<MPI.COMM_WORLD.Size(); i++){
			MPI.COMM_WORLD.Barrier();
			if(i==MPI.COMM_WORLD.Rank()){
				System.out.println("Process "+i+":");
				System.out.println("Mean centrality value = "+mean);
				for(Integer j: directory.directoryMap.keySet()){
					List<DocNode> l = directory.directoryMap.get(j);
					System.out.print("\nDirectory "+j+": ");
					for(DocNode d: l){
						System.out.print(d.fileName+" ");
					}
					System.out.println(" ");
				}
			}
		}
		MPI.COMM_WORLD.Barrier();

		return distributeInitialClusters(clusters.toArray());
	}

	//to gather initial clusters from all processors in the main processor
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
				c.changeNodeID(index); 
				clusterMap.put(c.nodeID, c);
				index++;
				System.out.println("\n Node(cluster) "+c.nodeID+" - representative points:");
				for(DocNode d: c.getRepPoints()){
					System.out.print(((DocNode)d).fileName+" ");
				}
			}
		}
		return clusterMap;
	}

	public Cluster mergeAllCluster(){
		List<Cluster> clusterList = new ArrayList(this.clustersAtThisLevel.values());
		/*for(Map.Entry<Long, Cluster> entry : this.clustersAtThisLevel.entrySet()){
			clusterList.add(entry.getValue());
		}*/
		return new Cluster(0,(List<? extends Node>) clusterList, 0.0);
	}		

}
//combo 1: 0.3f-sparsification Math.min mean+stdDev
//combo 2: 0.4f-sparsification Math.max mean+1.5*stdDev

/*for(List<Node> component: components){
if(component.size()>2){
System.out.println(" ");
component.forEach(n -> System.out.print(n.nodeID+" "));
Graph mst = graph.findMST(component);
float[] values = getMSTEdgeWeights(mst);
stats = new Statistics(values);
System.out.print("\n"+stats.getMean()+" "+stats.getStdDev()+" ");
mean+= stats.getMean();
stdDev+= stats.getStdDev();
count++;
}
}
mean/= count;
stdDev/= count;*/

