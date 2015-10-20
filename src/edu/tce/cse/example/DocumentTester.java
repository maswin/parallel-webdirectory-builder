package edu.tce.cse.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mpi.MPI;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;

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
	public static float findCosineSimilarity(Map<String, Double> tfIdf1, Map<String, Double> tfIdf2){
		double E = 0.0;
		double E1 = 0.0;
		double E2 = 0.0;
		Set<String> words = new HashSet<String>();
		words.addAll(tfIdf1.keySet());
		words.addAll(tfIdf2.keySet());
		for (String word : words) {
			if(tfIdf1.containsKey(word)){
				E1 += Math.pow(tfIdf1.get(word), 2);
			}
			if(tfIdf2.containsKey(word)){
				E2 += Math.pow(tfIdf2.get(word), 2);
			}
			if(tfIdf1.containsKey(word) && tfIdf2.containsKey(word)){
				E += tfIdf1.get(word)*tfIdf2.get(word);
			}			
		}
		E1 = Math.sqrt(E1);
		E2 = Math.sqrt(E2);
		E = (E / (E1*E2));
		return (float)(Math.abs(E));
	}
	public static void main(String args[]) throws IOException{ 

		MPI.Init(args);
		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		System.out.println("Started Id : "+id+"/"+size);

		DocumentInitializer DI = new DocumentInitializer("TestDocuments");
		List<DocNode> docList=new ArrayList<>();

		
		docList = DI.getDocNodeList();

		/*for(DocNode d : docList){
			System.out.println("File Neme : "+d.fileName);
			System.out.println("Actual Tf-Idf Vector : "+Arrays.toString(d.tfIdf));
			System.out.println("Reduced Tf-Idf Vector : "+Arrays.toString(d.reducedTfIdf));
			System.out.println();
		}*/
		//Testing
		printMatrix(docList);

		MPI.Finalize();

	}
	/*private static void printMatrix(List<DocNode> docList){
		double sim = 0.0;
		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		MPI.COMM_WORLD.Barrier();

		for(int i=0;i<size;i++){
			MPI.COMM_WORLD.Barrier();
			if(id==i){

				for(int j = -1;j<docList.size();j++){
						for(int k = -1;k<docList.size();k++){
							if(j==-1 && k==-1){
								System.out.printf("%12s","");
							}else if(k==-1){
					             System.out.printf("%12s ", (docList.get(j).fileName.length() > 12) ? docList.get(j).fileName.substring(0, 12) : docList.get(j).fileName);
							}else if(j==-1){
					             System.out.printf("%12s ", (docList.get(k).fileName.length() > 12) ? docList.get(k).fileName.substring(0, 12) : docList.get(k).fileName);

							}else{
								sim = docList.get(j).findEuclideanSimilarity(docList.get(k));
								System.out.printf("%.10f ",sim);
							}
						}
					System.out.println("");
				}
				System.out.println("");
			}
		}
	}*/
	private static void printMatrix(List<DocNode> docList){
		double sim = 0.0;
		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		MPI.COMM_WORLD.Barrier();

		for(int i=0;i<size;i++){
			MPI.COMM_WORLD.Barrier();
			if(id==i){

				System.out.println("Processor "+i);
				System.out.println("Documents Assigned : ");
				for(int j = 0;j<docList.size();j++){
						System.out.println(docList.get(j).fileName);
				}
				System.out.println("Total Number of Documents Assigned : "+docList.size());
				System.out.println("");
			}
		}
	}

}
