package edu.tce.cse.clustering;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.tce.cse.example.sampleData;
import edu.tce.cse.util.LSH;

public class LSHTester {
	public static void main(String args[]) throws IOException{
		List<Document> nodeList=new ArrayList<Document>();
		
		sampleData sd = new sampleData();
		nodeList = sd.getSampleDoc();
		
		LSH lsh = new LSH(6,10,nodeList.get(0).getSignatureVector().length);
		
		DisjointSet<Document> dSet = new DisjointSet<Document>();
		int[][] hash = new int[nodeList.size()][];
		int index = 0;
		for(Document node : nodeList){
			dSet.makeSet(node);
			hash[index] = lsh.hashSignature(node.getSignatureVector());
			
			System.out.println(node.getFilePath()+" "+Arrays.toString(hash[index]));
			
			index++;
		}
		List<List<Document>> sets;
		for(int i=0;i<nodeList.size();i++){
			for(int j=i+1;j<nodeList.size();j++){
				if(majorityMatch(hash[i],hash[j])){
					dSet.union(nodeList.get(i), nodeList.get(j));
				}
			}
		}
		sets = dSet.getAllSets();
		System.out.println("Sets Size : "+sets.size());
		for(List<Document> set : sets){
			for(Document d : set){
				System.out.println(d.getFilePath());
			}
			System.out.println();
		}
	}
	public static boolean majorityMatch(int[] hash1, int[] hash2){
		boolean result = false;
		int count = 0;
		for(int i=0;i<hash1.length;i++){
			if(hash1[i] == hash2[i]){
				count++;
			}
		}
		if(count >= (hash1.length/2) ){
			result = true;
		}
		return result;
	}
}
