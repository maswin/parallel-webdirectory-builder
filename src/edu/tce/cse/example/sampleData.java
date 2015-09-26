package edu.tce.cse.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mpi.MPI;
import mpi.Op;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;

public class sampleData {
	public static String documentDirectory = "/home/mukuntha/Downloads/TestDocuments";
	private List<Document> documentList;
	public sampleData(){
		documentList = new ArrayList<Document>();
		try {
			InitializeDocuments();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void InitializeDocuments() throws IOException{
		File inputDirectory = new File(documentDirectory);
        File[] files = inputDirectory.listFiles();
        
        Map<String, Integer> documentFrequency = new LinkedHashMap<String, Integer>();
        
        for (int i = 0; i < files.length; i++) {
            Document document = new Document(i, files[i].getAbsolutePath());
            document.parseDocument(documentFrequency);
            documentList.add(document);
        }
        
        for(Document doc : documentList){
        	doc.calculateTfIdf(files.length, documentFrequency);
        }
        
	}
	public List<Document> getSampleDoc() throws IOException{
		return documentList;
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
			node = new DocNode(docs.getDocID(), signature, docs.getTfIdf());
			docNodes.add(node);
		}
		return docNodes;
	}
	public List<DocNode> getSampleDocNodes(List<Document> inputDocuments) throws IOException{
		List<DocNode> docNodes = new ArrayList<DocNode>();
		
		boolean[] signature;
		DocNode node;
		for(Document docs : inputDocuments){
			signature = docs.getSignatureVector();
			node = new DocNode(docs.getDocID(), signature, docs.getTfIdf());
			node.setFileName(docs.getFilePath());
			docNodes.add(node);
		}
		return docNodes;
	}
}
