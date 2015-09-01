package edu.tce.cse.clustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.tce.cse.example.sampleData;

public class TryInitialClustering {
	public static void main(String args[]) throws IOException{
		
		List<DocNode> list=new ArrayList<DocNode>();
		/*for(int i=0; i<5; i++){
			boolean sig[]=new boolean[70];
			for(int j=0; j<70; j++){
				if(Math.random()<0.5)
					sig[j]=true;
				else
					sig[j]=false;
				System.out.print(sig[j]);
			}
			DocNode d=new DocNode(i, sig);
			list.add(d);
			System.out.println(" "+i);
		}*/
		
		sampleData sd = new sampleData();
		list = sd.getSampleDocNodes();
		InitialClusterer obj=new InitialClusterer(list);
		Graph<DocNode> graph=obj.findCentrality();
		for(int i=0; i<graph.V.size(); i++){
			System.out.println(graph.V.get(i).centrality);
		}
		
		
	}
}
