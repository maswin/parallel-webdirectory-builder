package edu.tce.cse.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.tce.cse.clustering.DocNode;

public class SimilarityTester {
	public static void main(String args[]) throws IOException{
		List<DocNode> list=new ArrayList<DocNode>();
		
		sampleData sd = new sampleData();
		list = sd.getSampleDocNodes();
		
		DocNode primary = list.get(0);
		double value1=0.0;
		double value2=0.0;
		System.out.println("Approximate Cosine Similarity Testing :");
		System.out.println("Document for Testing : Music.txt");
		/*for(DocNode node : list){
			System.out.println(primary.findCosSimilarity(node));
		}*/
		for(int i=1;i<8;i++){
			value1+=primary.findCosSimilarity(list.get(i));
			i++;
		}
		for(int i=8;i<15;i++){
			value2+=primary.findCosSimilarity(list.get(i));
			i++;
		}
		System.out.println("Similarity With Music Related Documents: "+value1);
		System.out.println("Similarity With War Related Documents: "+value2);
	}
}
