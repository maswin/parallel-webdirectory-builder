package edu.tce.cse.clustering;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TryInitialClustering {
	public static void main(String args[])throws Exception{
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		List<DocNode> list=new ArrayList<DocNode>();
		//modify number of signature vectors here
		for(long i=0; i<200; i++){
			String line=br.readLine();
			int sig[]=new int[line.length()];
			for(int j=0; j<sig.length; j++){
				sig[j]=line.charAt(j)-'0';
			}
			//DocNode d=new DocNode(i, sig);
			//list.add(d);
		}
		
		
		InitialClusterer obj=new InitialClusterer(list);
		obj.findCentrality();
		Graph<DocNode> graph=obj.graph;
		
		float sum=0;
		System.out.println("");
		System.out.println("Betweenness Centrality values:");
		for(int i=0; i<graph.V.size(); i++){
			System.out.println(graph.V.get(i).nodeID+": "+graph.V.get(i).centrality);
			sum+=graph.V.get(i).centrality;
		}
		float mean=sum/graph.V.size();
		double squareSum = 0;
		for (int i = 0; i < graph.V.size(); i++) {
		squareSum += Math.pow(graph.V.get(i).centrality- mean, 2);
		}
		float stdDev= (float)Math.sqrt((squareSum) / (graph.V.size() - 1));
		/*for(int range=1; range<=3; range++){
			System.out.println(" ");
			System.out.print("Lesser than mean+("+range+"*stdDev): ");
			for(int i=0; i<graph.V.size(); i++){
				float lowDiff=(range-1)*stdDev;
				float highDiff=range*stdDev;
				if((graph.V.get(i).centrality>(mean+lowDiff))&&(graph.V.get(i).centrality<=(mean+highDiff)))
					System.out.print(i+" ");
				//else if((graph.V.get(i).centrality<(mean-lowDiff)))//&&(graph.V.get(i).centrality>=(mean-highDiff)))
				//	System.out.print(i+" ");
			}
		}*/
		System.out.println(" ");
		System.out.println(" ");
		System.out.println("Vertices having centrality scores greater than or equal to mean: ");
		for(int i=0; i<graph.V.size(); i++){
			if(graph.V.get(i).centrality>=mean)
				System.out.print(i+" ");
		}
		obj.removeInterClusterEdges(mean);
		List<List<DocNode>> components = graph.findConnectedComponents();
		List<Cluster> clusters= obj.formClusters(components);
		for(Cluster c: clusters){
			System.out.println("\n Node(cluster) "+c.nodeID+" - representative points:");
			for(DocNode d: c.repPoints){
				System.out.print(d.nodeID+" ");
			}
		}
		
		
	}
}

