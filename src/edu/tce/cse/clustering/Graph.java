package edu.tce.cse.clustering;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.BitSet;



public class Graph<E extends Node> {
	List<E> V;
	Map<E, List<Edge>> adjList;
	public final static int NTHREAD = Runtime.getRuntime().availableProcessors();
	public Graph(List<E> nodes){
		V = new ArrayList<E>();
		adjList = new HashMap<E, List<Edge>>();
		V.addAll(nodes);
		adjList.put(nodes.get(0), new ArrayList<Edge>());
		for(int j=1; j<nodes.size(); j++){
			adjList.put(nodes.get(j), new ArrayList<Edge>());
			float weight = findEdgeWeight(nodes.get(0), nodes.get(j));
			Edge e = new Edge(nodes.get(0), nodes.get(j), weight);
			adjList.get(nodes.get(0)).add(e);
			e = new Edge(nodes.get(j), nodes.get(0), weight);
			adjList.get(nodes.get(j)).add(e);
		}
		for(int i=1; i<nodes.size(); i++){
			for(int j=i+1; j<nodes.size(); j++){
				float weight = findEdgeWeight(nodes.get(i), nodes.get(j));
				Edge e = new Edge(nodes.get(i), nodes.get(j), weight);
				adjList.get(nodes.get(i)).add(e);
				e = new Edge(nodes.get(j), nodes.get(i), weight);
				adjList.get(nodes.get(j)).add(e);
			}
		}
	}
	public float findEdgeWeight(E node1, E node2){
		return node1.findEdgeWEight(node2);
	}
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
	public List<List<DocNode>> findConnectedComponents(){
		BitSet bs = new BitSet(V.size());
		int count = 1;
		List<List<DocNode>> clusters=new ArrayList<List<DocNode>>();
		List<DocNode> cluster;
		for(int i=0; i<V.size(); i++){
			if(!bs.get(i)){
				System.out.println(" ");
				System.out.println("Component "+count+":");
				cluster=new ArrayList<DocNode>();
				findComponent(bs, (DocNode)V.get(i), cluster);
				clusters.add(cluster);
				count++;
				
			}
		}
		return clusters;
	}
	public void findComponent(BitSet bs, DocNode v, List<DocNode> cluster){
		System.out.print(v.nodeID+" ");
		bs.set((int)v.nodeID);
		cluster.add(v);
		for(int j=0; j<adjList.get(v).size(); j++){
			DocNode neighbour = (DocNode)adjList.get(v).get(j).getDst();
			if(!bs.get((int)neighbour.nodeID)){
				findComponent(bs, neighbour, cluster);
			}
		}
	}
	
}

class WeightComparator implements Comparator{
	public int compare(Object i, Object j){
		if(((Edge)i).getWeight()>((Edge)j).getWeight())
			return -1;
		else if(((Edge)i).getWeight()==((Edge)j).getWeight())
			return 0;
		return 1;
	}
}
class SparsifierRunnable<E extends Node> implements Runnable{
	int id;
	Map<E, List<Edge>> adjList;
	List<E> myNodes;
	int toRetain;
	public SparsifierRunnable(int id, Map<E, List<Edge>> adjList, List<E> myList, int toRetain){
		this.id=id;
		this.adjList=adjList;
		this.myNodes=myList;
		this.toRetain = toRetain;
	}
	public void run(){
		for(int i=0; i<myNodes.size(); i++){
			List<Edge> list=adjList.get(myNodes.get(i));
			Collections.sort(list, new WeightComparator());
			int d=list.size();
			list = list.subList(0, toRetain);
			//System.out.println(list.size());
			adjList.put(myNodes.get(i), list);
			//list=adjList.get(myNodes.get(i));
			//System.out.println("For "+myNodes.get(i).nodeID);
			
		}
	}
}

