package edu.tce.cse.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph<E extends Node> {
	List<E> V;
	Map<E, List<Edge>> adjList;
	Graph(List<E> nodes){
		V = new ArrayList<E>();
		adjList = new HashMap<E, List<Edge>>();
		V.addAll(nodes);
		adjList.put(nodes.get(0), new ArrayList<Edge>());
		for(int j=1; j<nodes.size(); j++){
			adjList.put(nodes.get(j), new ArrayList<Edge>());
			//float weight = findEdgeWeight(nodes.get(0), nodes.get(j));
			float weight = j*0.1f;
			//System.out.print("0 "+j+" "+weight+" ");
			Edge e = new Edge(nodes.get(0), nodes.get(j), weight);
			adjList.get(nodes.get(0)).add(e);
			e = new Edge(nodes.get(j), nodes.get(0), weight);
			adjList.get(nodes.get(j)).add(e);
		}
		for(int i=1; i<nodes.size(); i++){
			for(int j=i+1; j<nodes.size(); j++){
				//float weight = findEdgeWeight(nodes.get(i), nodes.get(j));
				float weight = 0.1f*(j-i);
				//System.out.print(i+" "+j+" "+weight+" ");
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
	
	

}
