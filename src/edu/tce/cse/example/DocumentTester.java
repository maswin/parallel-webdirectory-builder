package edu.tce.cse.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.tce.cse.clustering.DocNode;
import edu.tce.cse.clustering.Document;

public class DocumentTester {
	public static double findEucledianSimilarity(Map<String, Double> tfIdf1, Map<String, Double> tfIdf2){
		double E = 0.0;
		double E1 = 0.0;
		double E2 = 0.0;
		Set<String> words = new HashSet<String>();
		words.addAll(tfIdf1.keySet());
		words.addAll(tfIdf2.keySet());
		for (String word : words) {
			E1 = 0;
			E2 = 0;
			
			if(tfIdf1.containsKey(word)){
				E2 = tfIdf1.get(word);
			}
			if(tfIdf2.containsKey(word)){
				E1 = tfIdf2.get(word);
			}
			E += Math.pow((E1-E2), 2);
		}
		
		return (float)(Math.abs(Math.sqrt(E)));
	}
	public static double findCosineSimilarity(Map<String, Double> tfIdf1, Map<String, Double> tfIdf2){
		double E = 0.0;
		double E1 = 0.0;
		double E2 = 0.0;
		for (String word : tfIdf1.keySet()) {
			E1 += Math.pow(tfIdf1.get(word), 2);
			if(tfIdf2.containsKey(word)){
				E2 += Math.pow(tfIdf2.get(word), 2);
				E += tfIdf1.get(word)*tfIdf2.get(word);
			}
		}
		E1 = Math.sqrt(E1);
		E2 = Math.sqrt(E2);
		E = (E / (E1*E2));
		return (float)(Math.abs(E));
	}
	public static void main(String args[]) throws IOException{
		List<Document> list=new ArrayList<Document>();
		List<DocNode> nodeList=new ArrayList<DocNode>();
		
		sampleData sd = new sampleData();
		list = sd.getSampleDoc();
		nodeList = sd.getSampleDocNodes(list);
		
		Map<String, Double> termFreq = new HashMap<String, Double>();
		Document primary = list.get(0);
		DocNode primaryNode = nodeList.get(0);
		System.out.println("Cosine Distance Testing :");
		System.out.println("Document for Testing : "+primary.getFilePath());
		/*for(Document doc : list){
			System.out.println(findCosineSimilarity(primary.getTfIdfVector(),doc.getTfIdfVector()));
		}*/
		
		double tmp = 0.0;
		double tmp1 = 0.0;
		for(int i=1;i<list.size();i++){
			tmp = findCosineSimilarity(primary.getTfIdfVector(),list.get(i).getTfIdfVector());
			tmp1 = primaryNode.findCosSimilarity(nodeList.get(i));
			System.out.println(list.get(i).getFilePath()+" "+tmp+" "+tmp1);
		}
	}
}
