package edu.tce.cse.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.PriorityQueue;

public class InitialClusterer {
	public final static int NTHREAD = Runtime.getRuntime().availableProcessors(); // Number of threads to create
	List<DocNode> docs;
	Graph<DocNode> graph;
	public InitialClusterer(List<DocNode> list){
		docs = list;
	}
	public void findCentrality(){
		graph = new Graph(docs);
		//MODIFY SPARSIFICATION EXPONENT HERE
		graph.sparsify(0.2f);
		Thread[] threads = new Thread[NTHREAD];
		int share=(int)Math.ceil(docs.size()/NTHREAD);
		List<DocNode> myList;
		//DocNode[] minNodes = new DocNode[NTHREAD];
		//PriorityQueue<DocNode>[] queues = new FibonacciPriorityQueue[NTHREAD];
		PriorityQueue<DocNode>[] queues2 = new PriorityQueue[NTHREAD];
		for(int source = 0; source<graph.V.size(); source++){
			graph.V.get(source).level=0;
			graph.V.get(source).priority=0;
			graph.V.get(source).sig=1;
			HashMap<Integer, List<Edge<DocNode>>> edgesMap = new HashMap<Integer, List<Edge<DocNode>>>();	
			for (int i = 0; i < NTHREAD; i++) {
				if(i!=NTHREAD-1)
					myList = graph.V.subList(i*share, (i+1)*share);
				else
					myList = graph.V.subList(i*share, graph.V.size());
				threads[i] = new Thread(new ForwardPhaseRunnable(i, myList, graph, graph.V.get(source), new PriorityQueue<DocNode>(10, new PriorityComparator()) , queues2));
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
				for (int i = 0; i < NTHREAD; i++) {
					if(i!=NTHREAD-1)
						myList = graph.V.subList(i*share, (i+1)*share);
					else
						myList = graph.V.subList(i*share, graph.V.size());
					threads[i] = new Thread(new ForwardPhaseRunnable(i, myList, graph, nextNode, queues2[i], newQueues));
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
			for(int i=0; i<graph.V.size(); i++){
				Set<Integer> iterator = graph.V.get(i).pred.keySet();
				for(int j: iterator){
					if(!edgesMap.containsKey(j)){
						edgesMap.put(j, graph.V.get(i).pred.get(j));
					}
					else
						edgesMap.get(j).addAll( graph.V.get(i).pred.get(j));
				}
			}
			Set<Integer> keyset = edgesMap.keySet();
			List<Integer> levels = new ArrayList<Integer>();
			levels.addAll(keyset);
			Collections.sort(levels , new LevelComparator());
			for(int level: levels){
				for (int i = 0; i < NTHREAD; i++) {
					if(i!=NTHREAD-1)
						myList = graph.V.subList(i*share, (i+1)*share);
					else
						myList = graph.V.subList(i*share, graph.V.size());
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

			for(int v=0; v<graph.V.size(); v++){
				DocNode d = graph.V.get(v);
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
	
	public void removeInterClusterEdges(float threshold){
		Thread[] threads = new Thread[NTHREAD];
		List<DocNode> myList;
		int share = (int)Math.ceil(graph.V.size()/NTHREAD);
		Object lock=new Object();
		for (int i = 0; i < NTHREAD; i++) {
			if(i!=NTHREAD-1)
				myList = (List<DocNode>)graph.V.subList(i*share, (i+1)*share);
			else
				myList = (List<DocNode>)graph.V.subList(i*share, graph.V.size());
			threads[i] = new Thread(new EdgeRemoverRunnable(i, graph.adjList, myList, threshold, lock));
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
		for(int i=0; i<graph.V.size(); i++){
			List<Edge> list=graph.adjList.get(graph.V.get(i));
			for(int j=0; j<list.size(); j++){
				System.out.print("Edge ("+graph.V.get(i).nodeID+","+list.get(j).getDst().nodeID+") ");
			}
			System.out.println("");
			//System.out.println(" ");
		}
		
	}
	public List<Cluster> formClusters(List<List<DocNode>> list){
		Thread[] threads = new Thread[NTHREAD];
		List<List<DocNode>> myList;
		int share = (int)Math.ceil(list.size()/NTHREAD);
		Object lock=new Object();
		List<Cluster> clusters = new ArrayList<Cluster>();
		int nodeNum = graph.V.size();
		for (int i = 0; i < NTHREAD; i++) {
			if(i!=NTHREAD-1)
				myList = list.subList(i*share, (i+1)*share);
			else
				myList = list.subList(i*share, list.size());
			threads[i] = new Thread(new FormingClustersRunnable(nodeNum, myList, lock, clusters));
			nodeNum+=1;
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

class LevelComparator implements Comparator{
	public int compare(Object i, Object j){
		if((int)i>(int)j)
			return -1;
		return 1;
	}
}

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

class EdgeRemoverRunnable implements Runnable{
	int id;
	Map<DocNode, List<Edge>> adjList;
	List<DocNode> myNodes;
	float threshold;
	Object lock;
	public EdgeRemoverRunnable(int id, Map<DocNode, List<Edge>> adjList, List<DocNode> myList, float threshold, Object lock){
		this.id=id;
		this.adjList=adjList;
		this.myNodes=myList;
		this.threshold=threshold;
		this.lock=lock;
	}
	public void run(){
		List<Edge> toRemove=new LinkedList<Edge>();
		for(int i=0; i<myNodes.size(); i++){
			List<Edge> list=adjList.get(myNodes.get(i));
			for(Edge e: list){
				DocNode neighbour = (DocNode)(e.getDst());
				if((neighbour.centrality>threshold)&&(myNodes.get(i).centrality>threshold))
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


class FormingClustersRunnable implements Runnable{
	int id;
	List<List<DocNode>> components;
	Object lock;
	List<Cluster> clusters;
	public FormingClustersRunnable(int id, List<List<DocNode>> list, Object lock, List<Cluster> clusters){
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
