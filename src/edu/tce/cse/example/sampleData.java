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
import edu.tce.cse.util.SVDReducer;

public class sampleData {
	public static String documentDirectory = "TestDocuments";
	private List<Document> documentList;
	public sampleData(String directory){
		documentDirectory = directory;
		documentList = new ArrayList<Document>();
		try {
			List<File> files = new ArrayList<>();
			parseDirectory(documentDirectory, files);
			InitializeDocuments(files);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void parseDirectory(String documentDirectory, List files){
		File inputDirectory = new File(documentDirectory);
        File[] tmpFiles = inputDirectory.listFiles();
        for(File f : tmpFiles){
        	if(f.isDirectory()){
        		parseDirectory(f.getAbsolutePath(), files);
        	}else{
        		files.add(f);
        	}
        }
		
	}
	private void InitializeDocuments(List<File> files) throws IOException{

        
        Map<String, Integer> documentFrequency = new LinkedHashMap<String, Integer>();
        File f;
        for (int i = 0; i < files.size(); i++) {
        	f = files.get(i);
            Document document = new Document(i, f.getAbsolutePath(), f.getName());
            document.parseDocument(documentFrequency);
            documentList.add(document);
        }
        
       for(Document doc : documentList){
        	doc.calculateTfIdf(files.size(), documentFrequency);
        }
        //SVDReducer svd = new SVDReducer();
        //svd.reduceDocTfIdf(documentList);
        
	}
	public List<Document> getSampleDoc() throws IOException{
		return documentList;
	}
	
	public List<DocNode> getSampleDocNodes() throws IOException{
		List<DocNode> docNodes = new ArrayList<DocNode>();
		List<Document> inputDocuments = getSampleDoc();
		
		boolean[] signature;
		DocNode node;
		for(Document docs : inputDocuments){
			node = new DocNode(docs.getDocID(), docs.getFileName(), docs.getTfIdf());
			docNodes.add(node);
		}
		return docNodes;
	}
	public List<DocNode> getSampleDocNodes(List<Document> inputDocuments) throws IOException{
		List<DocNode> docNodes = new ArrayList<DocNode>();
		
		boolean[] signature;
		DocNode node;
		for(Document docs : inputDocuments){
			node = new DocNode(docs.getDocID(), docs.getFileName(), docs.getTfIdf());
			docNodes.add(node);
		}
		return docNodes;
	}
}
