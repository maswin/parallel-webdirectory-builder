package edu.tce.cse.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpi.MPI;
import edu.tce.cse.LSH.DistributedLSH;
import edu.tce.cse.LSH.LSH;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import edu.tce.cse.model.Centroid;
import edu.tce.cse.model.Data;

public class DistributedLSHTester {
	public static void main(String args[]) throws IOException{
		MPI.Init(args);

		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		System.out.println("Started Id : "+id+"/"+size);
		
		DocumentInitializer DI = new DocumentInitializer("TestDocuments");
		List<DocNode> docList=new ArrayList<>();
		List<Centroid> cList=new ArrayList<>();
		Map<Long, String> nameMap = new HashMap<>();
		docList = DI.getDocNodeList();
		
		for(DocNode d : docList){
			//System.out.println(d.nodeID+" "+d.fileName);
			Centroid c = new Centroid(d.nodeID,d.tfIdf);
			nameMap.put(d.nodeID, d.fileName);
			cList.add(c);
		}
		System.out.println("LSH Testing :");
		System.out.println("No. of Data points : "+docList.size());
		System.out.println("Actual number of comparisions : "+(docList.size()*docList.size()));
		System.out.println("Performing Hashing...");
		DistributedLSH dLSH = new DistributedLSH(cList.get(0).tfIdf.length);
		dLSH.hash(cList);
		System.out.println("Comparision Points : ");
		for(Data d : dLSH.getPairPoints()){
			System.out.println(nameMap.get(d.a)+" "+nameMap.get(d.b));
		}
		MPI.Finalize();
	}
}
