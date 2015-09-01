package edu.tce.cse.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.tce.cse.clustering.DocNode;
import edu.tce.cse.clustering.Document;

public class sampleData {
	public static String inputFolder = "TestDocuments";
	public List<Document> getSampleDoc() throws IOException{
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
		for(Document docs : inputDocuments){
			docs.getSignatureVector();
		}
		return inputDocuments;
	}
	public int[][] getSampleDocSignature() throws IOException{
	
		List<Document> inputDocuments = getSampleDoc();
		boolean[] signature;
		int[][] intSign = new int[inputDocuments.size()][];
		int i=0;
		int j=0;
		for(Document docs : inputDocuments){
			signature = docs.getSignatureVector();
			i=0;
			intSign[j] = new int[signature.length];
			for(boolean val: signature){
				if(val){
					intSign[j][i] = 1;
				}else{
					intSign[j][i] = 0;
				}
				i++;
			}
			System.out.println(Arrays.toString(intSign[j]));
			j++;
		}
		return intSign;
	}
	public List<DocNode> getSampleDocNodes() throws IOException{
		List<DocNode> docNodes = new ArrayList<DocNode>();
		List<Document> inputDocuments = getSampleDoc();
		
		boolean[] signature;
		DocNode node;
		for(Document docs : inputDocuments){
			signature = docs.getSignatureVector();
			node = new DocNode(docs.getDocID(), signature);
			docNodes.add(node);
		}
		return docNodes;
	}
}
