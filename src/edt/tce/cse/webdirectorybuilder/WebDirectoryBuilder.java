package edt.tce.cse.webdirectorybuilder;

import java.util.ArrayList;
import java.util.List;

import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import mpi.MPI;

public class WebDirectoryBuilder {
	public static void main(String args[]){
		MPI.Init(args);
		
		//Generate Data
		DocumentInitializer DI = new DocumentInitializer("TestDocuments");
		List<Document> docList = DI.getDocumentList();
		
		
		MPI.Finalize();
	}
}
