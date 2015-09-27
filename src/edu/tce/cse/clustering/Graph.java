package edu.tce.cse.clustering;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.BitSet;

import edu.tce.cse.document.DocNode;
import edu.tce.cse.util.DisjointSet;
import edu.tce.cse.util.KDTree;



public class Graph<E extends Node> {
	public List<E> V;
	public Map<E, List<Edge>> adjList;
	public final static int NTHREAD = Runtime.getRuntime().availableProcessors();
	public Graph(List<E> nodes){
		V = new ArrayList<E>();
		adjList = new HashMap<E, List<Edge>>();
		V.addAll(nodes);
	}
	
	//to add an edge from a->b and b->a 
	public void addEdge(E a, E b, float weight){
		Edge e = new Edge(a, b, weight);
		if(adjList.get(a)==null)
			adjList.put(a, new ArrayList<Edge>());
		adjList.get(a).add(e);
		e = new Edge(b, a, weight);
		if(adjList.get(b)==null)
			adjList.put(b, new ArrayList<Edge>());
		adjList.get(b).add(e);
	}
	
	//to form a complete graph 
	public void addEdges(){
		adjList.put(V.get(0), new ArrayList<Edge>());
		for(int j=1; j<V.size(); j++){
			adjList.put(V.get(j), new ArrayList<Edge>());
			float weight = findEdgeWeight(V.get(0), V.get(j));
			Edge e = new Edge(V.get(0), V.get(j), weight);
			adjList.get(V.get(0)).add(e);
			e = new Edge(V.get(j), V.get(0), weight);
			adjList.get(V.get(j)).add(e);
		}
		for(int i=1; i<V.size(); i++){
			for(int j=i+1; j<V.size(); j++){
				float weight = findEdgeWeight(V.get(i), V.get(j));
				Edge e = new Edge(V.get(i), V.get(j), weight);
				adjList.get(V.get(i)).add(e);
				e = new Edge(V.get(j), V.get(i), weight);
				adjList.get(V.get(j)).add(e);
			}
		}
	}
	
	public float findEdgeWeight(E node1, E node2){			
		return node1.findDistance(node2);
	}
	
	//to form a complete graph by calculating edge weight using KD tree LCA distance measure
	public void addEdges(List<KDTree> trees, HashMap<DocNode, Integer> nodeToTreeMap, int maxTreeHeight){
		adjList.put(V.get(0), new ArrayList<Edge>());
		Cluster c1 = (Cluster)V.get(0);
		Cluster c2;
		for(int j=1; j<V.size(); j++){
			c2 = (Cluster)V.get(j);
			adjList.put(V.get(j), new ArrayList<Edge>());
			float weight = c1.findDistanceUsingKDTreeMeasure(c2, trees, nodeToTreeMap, maxTreeHeight);
			Edge e = new Edge(V.get(0), V.get(j), weight);
			adjList.get(V.get(0)).add(e);
			e = new Edge(V.get(j), V.get(0), weight);
			adjList.get(V.get(j)).add(e);
		}
		for(int i=1; i<V.size(); i++){
			c1 = (Cluster)V.get(i);
			for(int j=i+1; j<V.size(); j++){
				c2 = (Cluster)V.get(j);
				float weight = c1.findDistanceUsingKDTreeMeasure(c2, trees, nodeToTreeMap, maxTreeHeight);
				Edge e = new Edge(V.get(i), V.get(j), weight);
				adjList.get(V.get(i)).add(e);
				e = new Edge(V.get(j), V.get(i), weight);
				adjList.get(V.get(j)).add(e);
			}
		}
	}

	//to sparsify the graph by retaining d^e edges (based on weight) for each node (d=degree of node) 
	public void sparsify(float e){
		Thread[] threads = new Thread[NTHREAD];
		List<DocNode> myList;
		int share = (int)Math.ceil(V.size()/NTHREAD);
		System.out.println("");
		System.out.println("");
		System.out.println("After sparsification: ");
		int d = V.size();
		int toRetain = (int)Math.abs(Math.pow(d, e));
		for (int i = 0; i < NTHREAD; i++) {
			if(i!=NTHREAD-1)
				myList = (List<DocNode>)V.subList(i*share, (i+1)*share);
			else
				myList = (List<DocNode>)V.subList(i*share, V.size());
			threads[i] = new Thread(new SparsifierRunnable(i, adjList, myList, toRetain));
			threads[i].start();
		}
		for (Thread thread : threads) {
			try {
				//A Wait for Joining all threads merging in a single Iteration
				thread.join();

			} catch (InterruptedException ee) {
				ee.printStackTrace();
			}
		}
		/*for(int i=0; i<V.size(); i++){
			List<Edge> list=adjList.get(V.get(i));
			for(int j=0; j<list.size(); j++){
				System.out.print("Edge ("+V.get(i).nodeID+","+list.get(j).getDst().nodeID+") ");
			}
			System.out.println("");
			//System.out.println(" ");
		}*/
	}

	//to perform depth first search and find connected components of the graph
	public List<List<DocNode>> findConnectedComponents(){
		BitSet bs = new BitSet(V.size());
		int count = 1;
		List<List<DocNode>> clusters=new ArrayList<List<DocNode>>();
		List<DocNode> cluster;
		for(int i=0; i<V.size(); i++){
			if(!bs.get(i)){
				//System.out.println("Component "+count+":");
				cluster=new ArrayList<DocNode>();
				findComponent(bs, (DocNode)V.get(i), cluster);
				clusters.add(cluster);
				count++;

			}
		}
		return clusters;
	}
	public void findComponent(BitSet bs, DocNode v, List<DocNode> cluster){
		bs.set((int)v.nodeID);
		cluster.add(v);
		for(int j=0; j<adjList.get(v).size(); j++){
			DocNode neighbour = (DocNode)adjList.get(v).get(j).getDst();
			if(!bs.get((int)neighbour.nodeID)){
				findComponent(bs, neighbour, cluster);
			}
		}
	}
	
	//to form MST using Kruskal's algorithm
	public Graph findMST(){
		
		Graph<E> mst = new Graph(V);
		DisjointSet<E> dSet = new DisjointSet();
		for(E node: V){
			dSet.makeSet(node);
		}
		List<Edge> edges = getEdgesIfCompleteGraph();
		edges.sort(new WeightComparator());
		int numEdges=0;
		for (Edge edge: edges) {
            /* If the endpoints are connected, skip this edge. */
            if (dSet.findSet((E) edge.getSrc()) == dSet.findSet((E) edge.getDst()))
                continue;

            /* Otherwise, add the edge. */
            mst.addEdge((E)edge.getSrc(), (E)edge.getDst(), edge.getWeight());

            /* Link the endpoints together. */
            dSet.union((E)edge.getSrc(), (E)edge.getDst());

            /* If we've added enough edges already, we can quit. */
            if (++numEdges == V.size()) break;
        }
		return mst;
	}
	
	public List<Edge> getEdgesIfCompleteGraph(){
		List<Edge> edges = new ArrayList();
		for(int i=0; i<V.size(); i++){
			for(int j=i+1; j<V.size(); j++){
				edges.add(adjList.get(V.get(i)).get(j-1));
			}
		}
		return edges;
	}
	
	//to run Brandes' algorithm to find centrality scores for each vertex
	public void findCentrality(){
		
		Thread[] threads = new Thread[NTHREAD];
		int share=(int)Math.ceil(V.size()/NTHREAD);
		List<DocNode> myList;
		//DocNode[] minNodes = new DocNode[NTHREAD];
		//PriorityQueue<DocNode>[] queues = new FibonacciPriorityQueue[NTHREAD];
		PriorityQueue<DocNode>[] queues2 = new PriorityQueue[NTHREAD];
		
		//fix each vertex in graph as source vertex
		for(int source = 0; source<V.size(); source++){
			DocNode src = (DocNode)(V.get(source));
			src.level=0;
			src.priority=0;
			src.sig=1;
			HashMap<Integer, List<Edge<DocNode>>> edgesMap = new HashMap<Integer, List<Edge<DocNode>>>();	
			
			//perform one iteration of Dijkstra's algo 
			for (int i = 0; i < NTHREAD; i++) {
				if(i!=NTHREAD-1)
					myList = (List<DocNode>) V.subList(i*share, (i+1)*share);
				else
					myList = (List<DocNode>) V.subList(i*share, V.size());
				threads[i] = new Thread(new ForwardPhaseRunnable(i, myList, (Graph<DocNode>)this, src, new PriorityQueue<DocNode>(10, new PriorityComparator()) , queues2));
				threads[i].start();
			}
			for (Thread thread : threads) {
				try {
					//A Wait for Joining all threads merging in a single Iteration
					thread.join();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			while(true){
				//decide which node is to be removed from queue next, by comparing min nodes in queue of each thread
				int count = 0;
				int next = -1;
				for(int i=0; i<NTHREAD; i++){

					if(queues2[i]==null||queues2[i].size()==0)
						count++;
					else{
						if(next==-1||queues2[i].peek().priority<queues2[next].peek().priority)
						{next=i; 
						}
					}
				}

				if(count==NTHREAD||next==-1)
					break;
				DocNode nextNode = queues2[next].poll();
				PriorityQueue<DocNode>[] newQueues = new PriorityQueue[NTHREAD];
				
				//perform an iteration of Dijkstra's with 'nextNode' 
				for (int i = 0; i < NTHREAD; i++) {
					if(i!=NTHREAD-1)
						myList = (List<DocNode>) V.subList(i*share, (i+1)*share);
					else
						myList = (List<DocNode>) V.subList(i*share, V.size());
					threads[i] = new Thread(new ForwardPhaseRunnable(i, myList, (Graph<DocNode>)this, nextNode, queues2[i], newQueues));
					threads[i].start();
				}
				for (Thread thread : threads) {
					try {
						//A Wait for Joining all threads merging in a single Iteration
						thread.join();

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				queues2 = newQueues;
			}
			
			//queues of all threads are empty now
			//combine pred of all nodes to form edgesMap (key = level, value = edges explored at that level)
			for(int i=0; i<V.size(); i++){
				Set<Integer> iterator = ((DocNode)V.get(i)).pred.keySet();
				for(int j: iterator){
					if(!edgesMap.containsKey(j)){
						edgesMap.put(j, ((DocNode)V.get(i)).pred.get(j));
					}
					else
						edgesMap.get(j).addAll(((DocNode)V.get(i)).pred.get(j));
				}
			}
			Set<Integer> keyset = edgesMap.keySet();
			List<Integer> levels = new ArrayList<Integer>();
			levels.addAll(keyset);
			Collections.sort(levels , new LevelComparator());
			
			//iterate beginning from the last level 
			for(int level: levels){
				//perform the backward phase
				for (int i = 0; i < NTHREAD; i++) {
					if(i!=NTHREAD-1)
						myList = (List<DocNode>) V.subList(i*share, (i+1)*share);
					else
						myList = (List<DocNode>) V.subList(i*share, V.size());
					threads[i] = new Thread(new BackwardPhaseRunnable(i, myList, edgesMap.get(level)));
					threads[i].start();
				}
				for (Thread thread : threads) {
					try {
						//A Wait for Joining all threads merging in a single Iteration
						thread.join();

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			//partial centrality values for all vertices when considering 'source' as source vertex have been computed
			//update centrality score for all vertices
			for(int v=0; v<V.size(); v++){
				DocNode d = (DocNode)V.get(v);
				d.centrality+=(d.delta);
				d.level=-1;
				d.delta=0;
				d.sig=0;
				d.priority=Float.MAX_VALUE;
				d.pred.clear();
			}
			/*System.out.println("Centrality values: Using "+source+" as source vertex in this iteration -");
			for(int i=0; i<graph.V.size(); i++){
				System.out.println(i+" "+graph.V.get(i).nodeID+" "+graph.V.get(i).centrality);
			}
			System.out.println();
			*/
		}
	}
	
	//to remove inter-cluster edges based on 'threshold' value
	public void removeInterClusterEdges(float threshold, boolean removeLessThanThreshold){
		Thread[] threads = new Thread[NTHREAD];
		List<DocNode> myList;
		int share = (int)Math.ceil(V.size()/NTHREAD);
		Object lock=new Object();
		for (int i = 0; i < NTHREAD; i++) {
			if(i!=NTHREAD-1)
				myList = (List<DocNode>)V.subList(i*share, (i+1)*share);
			else
				myList = (List<DocNode>)V.subList(i*share, V.size());
			threads[i] = new Thread(new EdgeRemoverRunnable(i, (Map<DocNode, List<Edge>>)adjList, myList, threshold, removeLessThanThreshold, lock));
			threads[i].start();
		}
		for (Thread thread : threads) {
			try {
				//A Wait for Joining all threads merging in a single Iteration
				thread.join();

			} catch (InterruptedException ee) {
				ee.printStackTrace();
			}
		}
		/*for(int i=0; i<V.size(); i++){
			List<Edge> list= adjList.get(V.get(i));
			for(int j=0; j<list.size(); j++){
				System.out.print("Edge ("+V.get(i).nodeID+","+list.get(j).getDst().nodeID+") ");
			}
			System.out.println("");
			//System.out.println(" ");
		}*/
		
	}
	
	//process each connected component to create Cluster objects
	public List<Cluster> formClusters(List<List<DocNode>> list, int startingClusterID){
		Thread[] threads = new Thread[NTHREAD];
		List<List<DocNode>> myList;
		int share = (int)Math.ceil(list.size()/NTHREAD);
		Object lock=new Object();
		List<Cluster> clusters = new ArrayList<Cluster>();
		for (int i = 0; i < NTHREAD; i++) {
			if(i!=NTHREAD-1)
				myList = list.subList(i*share, (i+1)*share);
			else
				myList = list.subList(i*share, list.size());
			threads[i] = new Thread(new FormingClustersRunnable(startingClusterID+(i*share), myList, lock, clusters));
			threads[i].start();
		}
		for (Thread thread : threads) {
			try {
				//A Wait for Joining all threads merging in a single Iteration
				thread.join();

			} catch (InterruptedException ee) {
				ee.printStackTrace();
			}
		}
		return clusters;
	}

}

//to sort edges in descending order of their weights
class WeightComparator implements Comparator{
	public int compare(Object i, Object j){
		if(((Edge)i).getWeight()>((Edge)j).getWeight())
			return -1;
		else if(((Edge)i).getWeight()==((Edge)j).getWeight())
			return 0;
		return 1;
	}
}

//to give a subset of V to each thread to perform sparsification
class SparsifierRunnable<E extends Node> implements Runnable{
	int id;
	Map<E, List<Edge>> adjList;
	List<E> myV;
	int toRetain;
	public SparsifierRunnable(int id, Map<E, List<Edge>> adjList, List<E> myList, int toRetain){
		this.id=id;
		this.adjList=adjList;
		this.myV=myList;
		this.toRetain = toRetain;
	}
	public void run(){
		for(int i=0; i<myV.size(); i++){
			List<Edge> list=adjList.get(myV.get(i));
			Collections.sort(list, new WeightComparator());
			int d=list.size();
			list = list.subList(0, toRetain);
			//System.out.println(list.size());
			adjList.put(myV.get(i), list);
			//list=adjList.get(myV.get(i));
			//System.out.println("For "+myV.get(i).nodeID);

		}
	}
}

//to sort levels in descending order
class LevelComparator implements Comparator{
	public int compare(Object i, Object j){
		if((int)i>(int)j)
			return -1;
		return 1;
	}
}

//to sort DocNode objects in ascending order of 'priority'
class PriorityComparator implements Comparator{
	public int compare(Object i, Object j){
		if(((DocNode)i).priority<((DocNode)j).priority)
			return -1;
		return 1;
	}
}

class ForwardPhaseRunnable implements Runnable{
	int id;
	List<DocNode> V;
	Graph<DocNode> graph;
	DocNode source;
	PriorityQueue<DocNode> queue;
	PriorityQueue<DocNode>[] queues;
	//DocNode[] minNodes;
	ForwardPhaseRunnable(int id, List<DocNode> V, Graph<DocNode> graph, DocNode source, PriorityQueue<DocNode> queue, PriorityQueue<DocNode>[] queues){
		this.id = id;
		this.V = V;
		this.graph = graph;
		this.source = source;
		this.queue = queue;
		//this.minNodes=minNodes;
		this.queues=queues;
	}
	public void run(){
		//Dijstra's algorithm
		if(queue!=null){
			for(int i=0; i<graph.adjList.get(source).size(); i++){
				DocNode v = (DocNode)graph.adjList.get(source).get(i).getDst();
				/*if(id==3)
					System.out.print("--"+v.nodeID+"--");*/
				if(V.contains(v)){
					float alt = graph.adjList.get(source).get(i).getWeight() + source.priority;
					if(v.priority==Float.MAX_VALUE){
						queue.add(v);
					}
					if(alt < v.priority){
						//queue.decreasePriority(v, alt);
						queue.remove(v);
						v.priority=alt;
						queue.add(v);
						v.pred.clear();
						v.sig = 0;
					}
					if(v.priority == alt){
						v.sig += source.sig;
						if(v.level<source.level+1){
							v.level = source.level+1;
						}
						if(!v.pred.containsKey(v.level)){
							v.pred.put(v.level, new ArrayList<Edge<DocNode>>());
						}
						v.pred.get(v.level).add((Edge<DocNode>)(graph.adjList.get(source).get(i)));
					}
				}
			}

			queues[id]=queue;
		}
	}
}

class BackwardPhaseRunnable implements Runnable{
	int id;
	List<DocNode> V;
	List<Edge<DocNode>> edges;
	public BackwardPhaseRunnable(int id, List<DocNode> V, List<Edge<DocNode>> edges){
		this.id=id;
		this.V=V;
		this.edges=edges;
	}
	public void run(){
		for(int i=0; i<edges.size(); i++){
			if(V.contains(edges.get(i).getSrc())){
				DocNode src = edges.get(i).getSrc();
				DocNode dst = edges.get(i).getDst();
				src.delta+=((src.sig/dst.sig)*(1+dst.delta));
				//System.out.println("For edge to "+dst.nodeID+" Updating delta of "+src.nodeID+" to "+src.delta);
			}
		}
	}
}

//to remove inter-cluster edges: decide if min or max of centrality values of an edge's nodes >= or < threshold
class EdgeRemoverRunnable implements Runnable{
	int id;
	Map<DocNode, List<Edge>> adjList;
	List<DocNode> myNodes;
	float threshold;
	Object lock;
	boolean removeLessThanThreshold;
	public EdgeRemoverRunnable(int id, Map<DocNode, List<Edge>> adjList, List<DocNode> myList, float threshold, boolean removeLessThanThreshold, Object lock){
		this.id=id;
		this.adjList=adjList;
		this.myNodes=myList;
		this.threshold=threshold;
		this.removeLessThanThreshold=removeLessThanThreshold;
		this.lock=lock;
	}
	public void run(){
		List<Edge> toRemove=new LinkedList<Edge>();
		for(int i=0; i<myNodes.size(); i++){
			List<Edge> list=adjList.get(myNodes.get(i));
			for(Edge e: list){
				DocNode neighbour = (DocNode)(e.getDst());
				float min=(float)Math.min(neighbour.centrality, myNodes.get(i).centrality);
				if(!removeLessThanThreshold&&min>=threshold)	
					toRemove.add(e);
				else if(removeLessThanThreshold&&min<threshold)	
					toRemove.add(e);
			}
		}
		synchronized(lock){
			for(Edge e: toRemove){
				adjList.get(e.getSrc()).remove(e);
				//System.out.println("removing ("+e.getSrc().nodeID+","+e.getDst().nodeID+")");
			}
		}
	}
}


class FormingClustersRunnable<E extends Node> implements Runnable{
	int id;
	List<List<E>> components;
	Object lock;
	List<Cluster> clusters;
	public FormingClustersRunnable(int id, List<List<E>> list, Object lock, List<Cluster> clusters){
		this.id = id;
		components = list;
		this.lock = lock;
		this.clusters = clusters;
	}
	public void run(){
		List<Cluster> list = new ArrayList<Cluster>();
		for(int i=0; i<components.size(); i++){
			Cluster c = new Cluster(id+i);
			c.formCluster(components.get(i));
			list.add(c);
		}
		synchronized(lock){
			clusters.addAll(list);
		}
	}
}

