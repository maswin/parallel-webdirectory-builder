package edu.tce.cse.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            Document document = new Document(i, files[i].getAbsolutePath());
            document.parseDocument(localDocumentFrequency[0]);
            documentList.add(document);
        }
        
        //MPI.COMM_WORLD.Barrier();
        /*
         * Collect Document Frequency from all Processors
         * Need not wait - Only when all processors approach 
         * reduce, reduce operation will take place.
         * Reduced Result will stay in Processor 0
         */
        
        Map<String, Integer>[] globalDocumentFrequency = new HashMap[1];
        MPI.COMM_WORLD.Reduce(localDocumentFrequency, 0,
                globalDocumentFrequency, 0, 1, MPI.OBJECT, new Op(new DocumentFrequencyReducer(), true), 0);
        
        //Broadcast the documentFrequency to other processor
        MPI.COMM_WORLD.Bcast(globalDocumentFrequency, 0, 1, MPI.OBJECT, 0);
        
        //System.out.println(this.processorID+" "+globalDocumentFrequency[0].keySet().size());
        
        for(Document doc : documentList){
        	doc.calculateTfIdf(N, globalDocumentFrequency[0]);
        }
        
	}

	public List<Document> getDocumentList() {
		return documentList;
	}

	
}
