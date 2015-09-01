package edu.tce.cse.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.tce.cse.betweenness.*;
public class InitialClusterer {
	public final static int NTHREAD = Runtime.getRuntime().availableProcessors(); // Number of threads to create
	List<DocNode> docs;
	public InitialClusterer(List<DocNode> list){
		docs = list;
	}
	public Graph<DocNode> findCentrality(){
		Graph<DocNode> graph = new Graph(docs);
		Thread[] threads = new Thread[NTHREAD];
		int share=docs.size()/NTHREAD;
		List<DocNode> myList;
		//DocNode[] minNodes = new DocNode[NTHREAD];
		PriorityQueue<DocNode>[] queues = new FibonacciPriorityQueue[NTHREAD];
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
				threads[i] = new Thread(new ForwardPhaseRunnable(i, myList, graph, graph.V.get(source), new FibonacciPriorityQueue(), queues));
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
			/*for(int i=0; i<graph.V.size(); i++){
				System.out.println(i+" "+graph.V.get(i).nodeID+" "+graph.V.get(i).priority);
			}*/
			while(true){
				//System.out.println("----");
				int count = 0;
				int next = -1;
				for(int i=0; i<NTHREAD; i++){
					
					if(queues[i]==null||queues[i].size()==0)
						count++;
					else{
						if(next==-1||queues[i].findMin().priority<queues[next].findMin().priority)
							next=i;
						//System.out.print(queues[i].size());
					}
				}
				if(count==NTHREAD)
					break;
				
				DocNode nextNode = queues[next].extractMin();
				//System.out.println("Next: "+nextNode.nodeID);
				PriorityQueue<DocNode>[] newQueues = new FibonacciPriorityQueue[NTHREAD];
				for (int i = 0; i < NTHREAD; i++) {
					if(i!=NTHREAD-1)
						myList = graph.V.subList(i*share, (i+1)*share);
					else
						myList = graph.V.subList(i*share, graph.V.size());
					threads[i] = new Thread(new ForwardPhaseRunnable(i, myList, graph, nextNode, queues[i], newQueues));
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
				queues = newQueues;
				/*for(int i=0; i<graph.V.size(); i++){
					System.out.println(i+" "+graph.V.get(i).nodeID+" "+graph.V.get(i).priority);
				}*/
			}
			for(int i=0; i<graph.V.size(); i++){
				//System.out.println(i+" "+graph.V.get(i).sig);
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
			Collections.sort(levels , new MyComparator());
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
				//System.out.print(d.delta+"--");
				d.level=-1;
				d.delta=0;
				d.sig=0;
				d.priority=Float.MAX_VALUE;
				d.pred.clear();
			}
			/*for(int i=0; i<graph.V.size(); i++){
			System.out.println(i+" "+graph.V.get(i).nodeID+" "+graph.V.get(i).centrality);
			*/
		}
		return graph;
	}
}

class MyComparator implements Comparator{
	public int compare(Object i, Object j){
		if((Integer)i>(Integer)j)
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
		/*int u = min.node;

			// find the neighbours
			if (neighbours[u] == null) {
				continue;
			}

			for (int i = 0; i < neighbours[u].length; ++i) {
				double alt = priorityObjectArray[u].priority + weights[u][i];
				if (alt < priorityObjectArray[neighbours[u][i]].priority) {
					priorityQueue.decreasePriority(priorityObjectArray[neighbours[u][i]], alt);
					previous[neighbours[u][i]] = u;
				}*/
		if(queue!=null){
			for(int i=0; i<graph.adjList.get(source).size(); i++){
				DocNode v = (DocNode)graph.adjList.get(source).get(i).getDst();
				if(V.contains(v)){
					float alt = graph.adjList.get(source).get(i).getWeight() + source.priority;
					if(v.priority==Float.MAX_VALUE){
						queue.add(v);
					}
					if(alt < v.priority){
						queue.decreasePriority(v, alt);
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
	
			//minNodes[id]=queue.findMin();
			queues[id]=queue;
			//System.out.println("Thread "+id+": "+queues[id].findMin().nodeID);
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

