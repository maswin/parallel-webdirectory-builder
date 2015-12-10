package edu.tce.cse.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
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
	private List<Long> docNodeList;
	
	public DocumentInitializer(String documentDirectory){
		this.documentDirectory = documentDirectory;
		List<File> files = new ArrayList<>();
		parseDirectory(documentDirectory, files);
		this.N = files.size();
		System.out.println("Size "+this.N);
		//Sort Files
		Collections.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2){
				return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
			}
		});
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

		List<Long> documentList = new ArrayList<>(endIndex - startIndex + 1);;

		Map<String, Integer>[] localDocumentFrequency = new LinkedHashMap[1];
		localDocumentFrequency[0] = new LinkedHashMap();


		//Sequential Code
		for (int i = startIndex; i <= endIndex; i++) {
            Document document = new Document(i, files.get(i).getAbsolutePath(), files.get(i).getName().trim());
            document.parseDocument(localDocumentFrequency[0]);
            DocMemManager.writeDocument(document);
            documentList.add(document.docID);
        }
		System.out.println("Initial Document Processing over");
		//Parallel Code 
		//Set Static Variables for DocumentParser Runnable
		/*int numOfCores = Runtime.getRuntime().availableProcessors();
		DocumentParser.documentList = documentList;
		DocumentParser.startIndex = this.startIndex;
		DocumentParser.endIndex = this.endIndex;
		DocumentParser.localDocumentFrequency = localDocumentFrequency[0];
		DocumentParser.numOfCores = numOfCores;
		DocumentParser.files = (File[]) files.toArray(new File[files.size()]);
		
		Object docLock = new Object();
		Object reduceLock = new Object();
		Thread[] threads = new Thread[numOfCores];
		for (int i = 0; i < numOfCores; i++) {
			threads[i] = new Thread(new DocumentParser(i, docLock, reduceLock));
			threads[i].start();
		}

		for (Thread thread : threads) {
			try {
				//A Wait for Joining all threads merging in a single Iteration
				thread.join();
			} catch (InterruptedException ee) {
				ee.printStackTrace();
			}
		}*/
		
		//Collect Document Frequency from all Processors
		Map<String, Integer>[] globalDocumentFrequency = new HashMap[1];
		MPI.COMM_WORLD.Allreduce(localDocumentFrequency, 0,
				globalDocumentFrequency, 0, 1, MPI.OBJECT, new Op(new DocumentFrequencyReducer(), true));

		//System.out.println(this.processorID+" "+globalDocumentFrequency[0].keySet().size());

		//Sequential Code
		if(MPI.COMM_WORLD.Rank()==0){
			System.out.println("Dimesnions : "+globalDocumentFrequency[0].size());
		}
		for(Long docID : documentList){
			Document doc = DocMemManager.getDocument(docID);
			doc.calculateTfIdf(this.N, globalDocumentFrequency[0]);
			DocMemManager.writeDocument(doc);
		}
		
		//Parallel Code
		//Set static variables for TfIdfCalc
		/*TfIdfCalc.documentList = documentList;
		TfIdfCalc.startIndex = this.startIndex;
		TfIdfCalc.endIndex = this.endIndex;
		TfIdfCalc.numOfCores = numOfCores;
		TfIdfCalc.numOfDoc = this.N;
		TfIdfCalc.docFrequencyMap = globalDocumentFrequency[0];
		
		for (int i = 0; i < numOfCores; i++) {
			threads[i] = new Thread(new TfIdfCalc(i));
			threads[i].start();
		}
		
		for (Thread thread : threads) {
			try {
				//A Wait for Joining all threads merging in a single Iteration
				thread.join();
			} catch (InterruptedException ee) {
				ee.printStackTrace();
			}
		}*/
		
		generateDocNodeList(documentList);
		System.out.println("Reduction Started");
		reduceTfIDF();
		System.out.println("Reduction Ended");
	}

	private void reduceTfIDF(){
		int tfIdfSize = DocMemManager.sampleNode.getTfIdf().size();
		//System.out.println(tfIdfSize);
		DoubleMatrix2D fullVector = new DenseDoubleMatrix2D(this.docNodeList.size(), tfIdfSize);		

		int index = 0;
		for(Long dId : this.docNodeList){
			DocNode doc = DocMemManager.getDocNode(dId);
			double[] tfIdf = doc.getTfIdf().toArray();
			for(int i=0;i<tfIdfSize;i++){
				fullVector.setQuick(index, i, tfIdf[i]);
			}
			fullVector.trimToSize();
			index++;
		}


		//Set reduced size
		//Default size
		int size = this.docNodeList.size();
		if(size >= 30)
			size = 30;

		//Reduce TfIdf
		SVDReducer svd = new SVDReducer(size);
		System.out.println(fullVector.viewRow(0).size());
		fullVector = svd.reduceTfIdf(fullVector);
		System.out.println(fullVector.viewRow(0).size());
		index = 0;
		for(Long dId : this.docNodeList){
			//double[] tfIdf = new double[tfIdfSize];
			DocNode doc = DocMemManager.getDocNode(dId);
			/*for(int i=0;i<size;i++){
				tfIdf[i] = fullVector.getQuick(index, i);
			}*/
			doc.setReducedTfIdf(new SparseDoubleMatrix1D(fullVector.viewRow(index).toArray()));
			DocMemManager.writeDocNode(doc);
			index++;
		}
	}

	private void parallelReduceTfIDF(){
		List<double[]>[] localTfIdf = new ArrayList[1];
		localTfIdf[0] = new ArrayList<double[]>(docNodeList.size());


		for( Long dId : docNodeList){
			DocNode doc = DocMemManager.getDocNode(dId);
			localTfIdf[0].add(doc.getTfIdf().toArray());
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
			//tfIdfMatrix = svd.reduceTfIdf(tfIdfMatrix);

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
		for(Long dId  : docNodeList){
			//System.out.println(this.processorID+" "+tfIdfMatrix[index].length);
			DocNode doc = DocMemManager.getDocNode(dId);
			doc.setTfIdf(new SparseDoubleMatrix1D(tfIdfMatrix[index]));
			index++;
		}
	}

	public List<Long> getDocNodeList() {
		return docNodeList;
	}

	public void generateDocNodeList(List<Long> documentList){
		DocNode node;
		for(Long dId : documentList){
			Document document = DocMemManager.getDocument(dId);
			node = new DocNode(document.getDocID(),document.getFileName(), document.getTfIdf());
			DocMemManager.writeDocNode(node);
			this.docNodeList.add(node.nodeID);
		}
		//reduceTfIDF();
		//reduceTfIDF(this.docNodeList);
	}

}
class DocumentParser implements Runnable{

	public static int startIndex;
	public static int endIndex;
	public static int numOfCores;
	public static List<Document> documentList;
	public static File[] files;
	public static Map<String, Integer> localDocumentFrequency;

	public int thisStartIndex;
	public int thisEndIndex;
	public List<Document> tmpDocumentList;
	public Object docLock;
	public Object reduceLock;
	public Map<String, Integer> tmpLocalDocumentFrequency;
	
	DocumentParser(int id, Object docLock, Object reduceLock){

		int N = endIndex - startIndex + 1;
		this.thisStartIndex = ((N / numOfCores) * id) + startIndex;
		this.thisEndIndex = ((id != numOfCores - 1)
				? (N / numOfCores) * (id + 1) - 1
						: N - 1) + startIndex;
		this.tmpDocumentList = new ArrayList<>(thisEndIndex - thisStartIndex + 1);
		this.tmpLocalDocumentFrequency = new LinkedHashMap<>();
		this.docLock = docLock;
		this.reduceLock = reduceLock;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			for(int i=thisStartIndex; i<=thisEndIndex;i++){
				Document document = new Document(i, files[i].getAbsolutePath(), files[i].getName().trim());
				document.parseDocument(tmpLocalDocumentFrequency);
				tmpDocumentList.add(document);
			}
			synchronized(docLock){
				documentList.addAll(tmpDocumentList);
			}
			synchronized(reduceLock){
				reduceMap();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	private void reduceMap(){
		for (String word : tmpLocalDocumentFrequency.keySet()) {
            int frequency = tmpLocalDocumentFrequency.get(word);
            if (DocumentParser.localDocumentFrequency.containsKey(word)) {
                frequency += DocumentParser.localDocumentFrequency.get(word);
            }
            DocumentParser.localDocumentFrequency.put(word, frequency);
        }
	}

}
class TfIdfCalc implements Runnable{

	public static int startIndex;
	public static int endIndex;
	public static int numOfCores;
	public static List<Document> documentList;
	public static int numOfDoc;
	public static Map<String, Integer> docFrequencyMap;
	
	public int thisStartIndex;
	public int thisEndIndex;
	
	TfIdfCalc(int id){
		int N = endIndex - startIndex + 1;
		this.thisStartIndex = ((N / numOfCores) * id) + startIndex;
		this.thisEndIndex = ((id != numOfCores - 1)
				? (N / numOfCores) * (id + 1) - 1
						: N - 1) + startIndex;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(int i=thisStartIndex; i<=thisEndIndex;i++){
			documentList.get(i).calculateTfIdf(numOfDoc, docFrequencyMap);
		}
		
	}
	
}
