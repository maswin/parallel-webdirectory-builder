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
        
        documentList.parallelStream().forEach(doc -> 
    		doc.generateSignature());
        
        //reduceTfIDF();
        
	}

	private void reduceTfIDF(){
		List<double[]>[] localTfIdf = new ArrayList[1];
		localTfIdf[0] = new ArrayList<double[]>(documentList.size());
		
		
		for( Document doc : documentList){
			localTfIdf[0].add(doc.getTfIdf());
		}
		
		//MPI.COMM_WORLD.Reduce(localTfIdf, 0, 
				//globalTfIdf, 0, 1, MPI.OBJECT, new Op(new TfIdfReducer(),false), 0);

		
		double[][] tfIdfMatrix;
		int index = 0;
		if(MPI.COMM_WORLD.Rank() == 0){
			//Initialize globalTfIdf
			List<double[]>[] globalTfIdf = new ArrayList[1];
			globalTfIdf[0] = new ArrayList<double[]>(N);
			
			//Collect TfIdf
			globalTfIdf[0].addAll(localTfIdf[0]);
			for(int i=1;i<MPI.COMM_WORLD.Size();i++){
				localTfIdf[0] = new ArrayList<double[]>();
				MPI.COMM_WORLD.Recv(localTfIdf, 0, 1, MPI.OBJECT, i, i);
				globalTfIdf[0].addAll(localTfIdf[0]);
			}
			
			//Create TfIdfMatrix
			tfIdfMatrix = new double[N][];
			for(double[] tfIdf : globalTfIdf[0]){
				tfIdfMatrix[index] = tfIdf;
				index++;
			}
			
			//Reduce TfIdf
			SVDReducer svd = new SVDReducer(30);
			tfIdfMatrix = svd.reduceTfIdf(tfIdfMatrix);
			
			//Send Reduced TfIdf
			for(int i=1;i<MPI.COMM_WORLD.Size();i++){
				int offset = (N / numberOfProcessors) * i;
		        int count = (i != numberOfProcessors - 1)
		                ? (N / numberOfProcessors) * (i + 1) - 1
		                : N - 1;
		       count = count - offset + 1;
		       MPI.COMM_WORLD.Send(tfIdfMatrix, offset, count, MPI.OBJECT, i, i);
			}
		}else{
			
			//Send TfIdf
			MPI.COMM_WORLD.Send(localTfIdf, 0, 1, MPI.OBJECT, 0, this.processorID);
			
			//Receive Reduced TfIdf
			int count = this.endIndex - this.startIndex + 1;
			tfIdfMatrix = new double[count][];
			MPI.COMM_WORLD.Recv(tfIdfMatrix, 0, count, MPI.OBJECT, 0, this.processorID);
		}
		index = 0;
		for(Document doc : documentList){
			//System.out.println(this.processorID+" "+tfIdfMatrix[index].length);
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
