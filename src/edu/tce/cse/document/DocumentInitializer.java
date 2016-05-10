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

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix1D;
import edu.tce.cse.util.SVDReducer;
import mpi.MPI;
import mpi.Op;

public class DocumentInitializer {
	private final int totalNumberOfFiles;
	private final String documentDirectory;
	private final int processorID;
	private final int numberOfProcessors;
	private final int startIndex;
	private final int endIndex;
	private List<DocNode> docNodeList;

	public DocumentInitializer(String documentDirectory){
		this.documentDirectory = documentDirectory;
		List<File> files = getAllFilesInADirectory(documentDirectory);
		this.totalNumberOfFiles = files.size();

		//Sort Files
		Collections.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2){
				return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
			}
		});

		this.processorID = MPI.COMM_WORLD.Rank();
		this.numberOfProcessors = MPI.COMM_WORLD.Size();
		this.startIndex = (totalNumberOfFiles / numberOfProcessors) * processorID;
		this.endIndex = (processorID != numberOfProcessors - 1) ?
				(totalNumberOfFiles / numberOfProcessors) * (processorID + 1) - 1 : totalNumberOfFiles - 1;

		this.docNodeList = new ArrayList<>(endIndex - startIndex + 1);
		try {
			InitializeDocuments(files);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<File> getAllFilesInADirectory(String docuemtnDirectory) {
		List<File> files = new ArrayList<>();
		getAllFilesInADirectory(documentDirectory, files);
		return files;
	}

	private void getAllFilesInADirectory(String documentDirectory, List files){
		File inputDirectory = new File(documentDirectory);
		File[] tmpFiles = inputDirectory.listFiles();
		for(File f : tmpFiles){
			if(f.isDirectory()){
				getAllFilesInADirectory(f.getAbsolutePath(), files);
			}else{
				files.add(f);
			}
		}		
	}

	private void InitializeDocuments(List<File> files) throws IOException{
		File inputDirectory = new File(documentDirectory);

		List<Document> documents = new ArrayList<>(endIndex - startIndex + 1);;

		Map<String, Integer>[] localDocumentFrequency = new LinkedHashMap[1];
		localDocumentFrequency[0] = new LinkedHashMap();

		//Sequential Code (Look at git for parallel version)
		for (int i = startIndex; i <= endIndex; i++) {
			Document document = new Document(i, files.get(i).getAbsolutePath(), files.get(i).getName().trim());
			document.parseDocument(localDocumentFrequency[0]);
			documents.add(document);
		}

		//Collect Document Frequency from all Processors
		Map<String, Integer>[] globalDocumentFrequency = new HashMap[1];
		MPI.COMM_WORLD.Allreduce(localDocumentFrequency, 0,
				globalDocumentFrequency, 0, 1, MPI.OBJECT, new Op(new DocumentFrequencyReducer(), true));

		//Sequential Code
		for(Document document : documents){
			document.calculateTfIdf(this.totalNumberOfFiles, globalDocumentFrequency[0]);
		}

		generateDocNodeList(documents);
	}

	private void generateDocNodeList(List<Document> documentList){
		DocNode node;
		for(Document document : documentList){
			node = new DocNode(document.getDocID(),document.getFileName(), document.getTfIdf());
			this.docNodeList.add(node);
		}

		reduceTfIDFInOneProcessor();
	}
	
	private void reduceTfIDFInOneProcessor(){
		double[][] tfIdfMatrix = new double[docNodeList.size()][];		

		for(int i=0; i<docNodeList.size(); i++){
			tfIdfMatrix[i] = docNodeList.get(i).getTfIdf().toArray();
		}

		//Set reduced size
		//Default size
		int size = docNodeList.size();
		if(docNodeList.size() >= 30)
			size = 30;

		//Reduce TfIdf
		SVDReducer svd = new SVDReducer(size);
		//tfIdfMatrix = svd.reduceTfIdf(tfIdfMatrix);

		for(int i=0; i<docNodeList.size(); i++){
			docNodeList.get(i).setReducedTfIdf(new DenseDoubleMatrix1D(tfIdfMatrix[i]));
		}
	}

	private void reduceTfIDFAcrossAllProcessor(){
		List<double[]>[] localTfIdf = new ArrayList[1];
		localTfIdf[0] = new ArrayList<double[]>(docNodeList.size());

		for(DocNode doc : docNodeList){
			localTfIdf[0].add(doc.getTfIdf().toArray());
		}

		//MPI.COMM_WORLD.Reduce(localTfIdf, 0, 
		//globalTfIdf, 0, 1, MPI.OBJECT, new Op(new TfIdfReducer(),false), 0);


		double[][] tfIdfMatrix;
		int index = 0;
		if(MPI.COMM_WORLD.Rank() == 0){
			//Initialize globalTfIdf
			List<double[]>[] globalTfIdf = new ArrayList[1];
			globalTfIdf[0] = new ArrayList<double[]>(totalNumberOfFiles);

			//Collect TfIdf
			globalTfIdf[0].addAll(localTfIdf[0]);
			for(int i=1;i<MPI.COMM_WORLD.Size();i++){
				localTfIdf[0] = new ArrayList<double[]>();
				MPI.COMM_WORLD.Recv(localTfIdf, 0, 1, MPI.OBJECT, i, i);
				globalTfIdf[0].addAll(localTfIdf[0]);
			}

			//Create TfIdfMatrix
			tfIdfMatrix = new double[totalNumberOfFiles][];
			for(double[] tfIdf : globalTfIdf[0]){
				tfIdfMatrix[index] = tfIdf;
				index++;
			}

			//Reduce TfIdf
			SVDReducer svd = new SVDReducer(30);
			tfIdfMatrix = svd.reduceTfIdf(tfIdfMatrix);

			//Send Reduced TfIdf
			for(int i=1;i<MPI.COMM_WORLD.Size();i++){
				int offset = (totalNumberOfFiles / numberOfProcessors) * i;
				int count = (i != numberOfProcessors - 1)
						? (totalNumberOfFiles / numberOfProcessors) * (i + 1) - 1
								: totalNumberOfFiles - 1;
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
			doc.setTfIdf(new SparseDoubleMatrix1D(tfIdfMatrix[index]));
			index++;
		}
	}

	public List<DocNode> getDocNodeList() {
		return docNodeList;
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
