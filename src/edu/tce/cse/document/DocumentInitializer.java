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
    private List<DocNode> docNodeList;
	public DocumentInitializer(String documentDirectory){
		this.documentDirectory = documentDirectory;
		List<File> files = new ArrayList<>();
		parseDirectory(documentDirectory, files);
        this.N = files.size();
        this.processorID = MPI.COMM_WORLD.Rank();
        this.numberOfProcessors = MPI.COMM_WORLD.Size();
        this.startIndex = (N / numberOfProcessors) * processorID;
        this.endIndex = (processorID != numberOfProcessors - 1)
                ? (N / numberOfProcessors) * (processorID + 1) - 1
                : N - 1;
       this.docNodeList = new ArrayList<>(endIndex - startIndex + 1);
       try {
    	   InitializeDocuments(files);
       } catch (IOException e) {
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
		File inputDirectory = new File(documentDirectory);
        
        List<Document> documentList = new ArrayList<>(endIndex - startIndex + 1);;
        
        Map<String, Integer>[] localDocumentFrequency = new LinkedHashMap[1];
        localDocumentFrequency[0] = new LinkedHashMap();
        
        for (int i = startIndex; i <= endIndex; i++) {
            Document document = new Document(i, files.get(i).getAbsolutePath(), files.get(i).getName());
            document.parseDocument(localDocumentFrequency[0]);
            documentList.add(document);
        }
       
        //Collect Document Frequency from all Processors
        Map<String, Integer>[] globalDocumentFrequency = new HashMap[1];
        MPI.COMM_WORLD.Allreduce(localDocumentFrequency, 0,
                globalDocumentFrequency, 0, 1, MPI.OBJECT, new Op(new DocumentFrequencyReducer(), true));
        
        //System.out.println(this.processorID+" "+globalDocumentFrequency[0].keySet().size());
        
        documentList.stream().forEach(doc -> 
        	doc.calculateTfIdf(N, globalDocumentFrequency[0]));
        
        generateDocNodeList(documentList);
	}

	private void reduceTfIDF(List<DocNode> documentList){
		double[][] tfIdfMatrix = new double[documentList.size()][];		

		for(int i=0; i<documentList.size(); i++){
			tfIdfMatrix[i] = documentList.get(i).getTfIdf();
		}
		
		
		//Set reduced size
		//Default size
		int size = documentList.size();
		if(documentList.size() >= 30)
			size = 30;
		
		//Reduce TfIdf
		SVDReducer svd = new SVDReducer(size);
		//tfIdfMatrix = svd.reduceTfIdf(tfIdfMatrix);
		
		for(int i=0; i<documentList.size(); i++){
			documentList.get(i).setReducedTfIdf(tfIdfMatrix[i]);
		}
	}
	
	private void reduceTfIDF(){
		List<double[]>[] localTfIdf = new ArrayList[1];
		localTfIdf[0] = new ArrayList<double[]>(docNodeList.size());
		
		
		for( DocNode doc : docNodeList){
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
		for(DocNode doc : docNodeList){
			//System.out.println(this.processorID+" "+tfIdfMatrix[index].length);
			doc.setTfIdf(tfIdfMatrix[index]);
			index++;
		}
	}
	
	public List<DocNode> getDocNodeList() {
		return docNodeList;
	}

	public void generateDocNodeList(List<Document> documentList){
		DocNode node;
		for(Document document : documentList){
			node = new DocNode(document.getDocID(),document.getFileName(), document.getTfIdf());
			this.docNodeList.add(node);
		}
		//reduceTfIDF();
		reduceTfIDF(this.docNodeList);
	}
	
}
