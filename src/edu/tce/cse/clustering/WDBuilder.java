package edu.tce.cse.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WDBuilder {
	public static void main(String args[]) throws IOException{
		System.out.println("Parallel & Scalable Web Directory Builder");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Enter the Input Directory :");
		String inputFolder = in.readLine();
		
		File folder = new File(inputFolder);
		File[] listOfFiles = folder.listFiles();

		List<Document> inputDocuments = new ArrayList<Document>();
		Document doc;
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        doc = new Document(file.getAbsolutePath());
		        inputDocuments.add(doc);
		    }
		}
		boolean[] signature;
		for(Document docs : inputDocuments){
			signature = docs.getSignatureVector();
		}
	}
}
