package edu.tce.cse.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mpi.MPI;
import edu.tce.cse.LSH.DistributedLSH;
import edu.tce.cse.LSH.LSH;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import edu.tce.cse.model.Data;

public class DistributedLSHTester {
	public static void main(String args[]) throws IOException{
		MPI.Init(args);

		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		System.out.println("Started Id : "+id+"/"+size);
		
		DocumentInitializer DI = new DocumentInitializer("TestDocuments");
		List<DocNode> docList=new ArrayList<>();
		
		docList = DI.getDocNodeList();
		
		/*double dist;
		double maxDist = Double.MIN_VALUE;
		for(int i=0;i<docList.size();i++){
			for(int j=i+1;j<docList.size();j++){
				dist = docList.get(i).findEuclideanSimilarity(docList.get(j));
				//System.out.println(dist);
				if(dist>maxDist){
					maxDist = dist;
				}
			}
		}*/
		//System.out.println(maxDist);
		//LSH DLHS = new LSH(docList.get(0).getSignature().length,7,maxDist/2);
		DistributedLSH dLSH = new DistributedLSH(docList.get(0).signature.length);
		dLSH.hash(docList);
		int count = 1;
		if(MPI.COMM_WORLD.Rank()==0){
			List<Data> data = dLSH.getPairPoints();
			for(Data d : data){
				System.out.println(count+" . "+d.a.fileName+"\t"+d.b.fileName);
				count++;
			}
		}
		
		dLSH.hash(docList);
		count = 1;
		if(MPI.COMM_WORLD.Rank()==0){
			List<Data> data = dLSH.getPairPoints();
			for(Data d : data){
				System.out.println(count+" . "+d.a.fileName+"\t"+d.b.fileName);
				count++;
			}
		}
		MPI.Finalize();
	}
}
