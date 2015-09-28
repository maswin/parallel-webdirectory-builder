package edu.tce.cse.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.tce.cse.util.SVDReducer;
import mpi.MPI;
import mpi.Op;

public class DocumentInitializer {
	private final int N;
    private final String documentDirectory;
    private final int processorID;
    private final int numberOfProcessors;
    private final int startIndex;
    private final int endIndex;
    private List<Document> documentList;
	public DocumentInitializer(String documentDirectory){
		this.documentDirectory = documentDirectory;
        this.N = new File(documentDirectory).listFiles().length;
        this.processorID = MPI.COMM_WORLD.Rank();
        this.numberOfProcessors = MPI.COMM_WORLD.Size();
        this.startIndex = (N / numberOfProcessors) * processorID;
        this.endIndex = (processorID != numberOfProcessors - 1)
                ? (N / numberOfProcessors) * (processorID + 1) - 1
                : N - 1;
       this.documentList = new ArrayList<>(endIndex - startIndex + 1);
       try {
    	   InitializeDocuments();
       } catch (IOException e) {
    	   e.printStackTrace();
       }
	}
	
	private void InitializeDocuments() throws IOException{
		File inputDirectory = new File(documentDirectory);
        File[] files = inputDirectory.listFiles();
        
        Map<String, Integer>[] localDocumentFrequency = new LinkedHashMap[1];
        localDocumentFrequency[0] = new LinkedHashMap();
        
        for (int i = startIndex; i <= endIndex; i++) {
            Document document = new Document(i, files[i].getAbsolutePath(), files[i].getName());
            document.parseDocument(localDocumentFrequency[0]);
            documentList.add(document);
        }
       
        //Collect Document Frequency from all Processors
        Map<String, Integer>[] globalDocumentFrequency = new HashMap[1];
        MPI.COMM_WORLD.Allreduce(localDocumentFrequency, 0,
                globalDocumentFrequency, 0, 1, MPI.OBJECT, new Op(new DocumentFrequencyReducer(), true));
        
        //System.out.println(this.processorID+" "+globalDocumentFrequency[0].keySet().size());
        
        documentList.parallelStream().forEach(doc -> 
        	doc.calculateTfIdf(N, globalDocumentFrequency[0]));
        
        reduceTfIDF();
        
	}

	private void reduceTfIDF(){
		List<double[]>[] localTfIdf = new ArrayList[1];
		localTfIdf[0] = new ArrayList<double[]>();
		
		List<double[]>[] globalTfIdf = new ArrayList[1];
		globalTfIdf[0] = new ArrayList<double[]>();
		
		for( Document doc : documentList){
			localTfIdf[0].add(doc.getTfIdf());
		}
		
		MPI.COMM_WORLD.Reduce(localTfIdf, 0, 
				globalTfIdf, 0, 1, MPI.OBJECT, new Op(new TfIdfReducer(),true), 0);
		int size[] = new int[1];
		size[0] = 0;
		if(MPI.COMM_WORLD.Rank()==0){
			size[0] = globalTfIdf[0].size();
		}
		MPI.COMM_WORLD.Bcast(size, 0, 1, MPI.INT, 0);
		
		double[][] tfIdfMatrix = new double[size[0]][];
		
		if(MPI.COMM_WORLD.Rank() == 0){
			int index = 0;
			for(double[] tfIdf : globalTfIdf[0]){
				tfIdfMatrix[index] = tfIdf;
				index++;
			}
			SVDReducer svd = new SVDReducer();
			tfIdfMatrix = svd.reduceTfIdf(tfIdfMatrix);
		}
		
		MPI.COMM_WORLD.Bcast(tfIdfMatrix, 0, size[0], MPI.OBJECT, 0);
		int index = this.startIndex;
		for(Document doc : documentList){
			doc.setTfIdf(tfIdfMatrix[index]);
			index++;
		}
	}
	public List<Document> getDocumentList() {
		return documentList;
	}

	public List<DocNode> getDocNodeList(){
		List<DocNode> docNodeList = new ArrayList<DocNode>();
		DocNode node;
		for(Document document : documentList){
			node = new DocNode(document.getDocID(),document.getFileName(),document.getSignatureVector(), document.getTfIdf());
			docNodeList.add(node);
		}
		return docNodeList;
	}
	
}
