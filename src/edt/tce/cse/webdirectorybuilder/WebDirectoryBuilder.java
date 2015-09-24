package edt.tce.cse.webdirectorybuilder;

import java.util.ArrayList;
import java.util.List;

import edu.tce.cse.clustering.Graph;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import mpi.MPI;

public class WebDirectoryBuilder {
	public static final String inputFolder = "TestDocuments";
	public void initialClustering(List<DocNode> nodeList){
		Graph<DocNode> graph = new Graph(nodeList);
		graph.addEdges();
		//modify sparsification exponent here
		graph.sparsify(0.3f);
		graph.findCentrality();
	}
	public static void main(String args[]){
		MPI.Init(args);
		
		//Generate Data - Set of Documents for each Processor
		DocumentInitializer DI = new DocumentInitializer(inputFolder);
		List<DocNode> docNodeList = DI.getDocNodeList();
		
		//Perform Initial Clustering
		
		MPI.Finalize();
	}
}
