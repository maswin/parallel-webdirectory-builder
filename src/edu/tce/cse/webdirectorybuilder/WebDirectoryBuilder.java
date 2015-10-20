package edu.tce.cse.webdirectorybuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.tce.cse.LSH.DistributedLSH;
import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Graph;
import edu.tce.cse.clustering.HierarchicalClustering;
import edu.tce.cse.clustering.Node;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.document.Document;
import edu.tce.cse.document.DocumentInitializer;
import edu.tce.cse.model.Data;
import edu.tce.cse.model.Directory;
import edu.tce.cse.util.Statistics;
import gui.TreeView;
import mpi.MPI;

public class WebDirectoryBuilder {
	public static final String inputFolder = "TestDocuments";
	
	
	public static void main(String args[]) throws FileNotFoundException{
		//fix threshold for number of clusters
				int k = 1; 
				//gather Clusters (initial) from all processors
				long startTime = System.currentTimeMillis();
				MPI.Init(args);
				int id = MPI.COMM_WORLD.Rank();
				int size = MPI.COMM_WORLD.Size();
				Directory directory = new Directory();
				System.out.println("Started Id : "+id+"/"+size);
				HierarchicalClustering hc = new HierarchicalClustering();

				List<DocNode> nodeList = hc.preprocess();
				System.out.println("Processor "+MPI.COMM_WORLD.Rank()+" ---Data Received---");
				DistributedLSH dLSH = new DistributedLSH(nodeList.get(0).tfIdf.length);

				hc.clustersAtThisLevel = hc.initialClustering(nodeList, directory);
				
				int clustersInPreviousLevel = hc.clustersAtThisLevel.size();
				int startID = hc.clustersAtThisLevel.size();
				int iteration = 1;
				
				while(true){
					MPI.COMM_WORLD.Barrier();
					clustersInPreviousLevel = hc.clustersAtThisLevel.size();
					List<Cluster> temp = new ArrayList<Cluster>();
					temp.addAll(hc.clustersAtThisLevel.values());
					
					hc.distributeCentroids(temp);
					temp.clear();
					MPI.COMM_WORLD.Barrier();
					if(MPI.COMM_WORLD.Rank()==0)
						System.out.println("\nCentroids are distributed");

					dLSH.hash(hc.centroids);
					if(MPI.COMM_WORLD.Rank()==0)
						System.out.println("Centroid points are hashed");

					if(MPI.COMM_WORLD.Rank()==0){
						List<Data> data = dLSH.getPairPoints();
						hc.mergeClusters(data, startID);
						startID+=hc.clustersAtThisLevel.size();
						System.out.println("--Merging of clusters--");
						
					   System.out.println("\n \n");
						for(Cluster c: hc.clustersAtThisLevel.values()){
							System.out.println("\n Cluster "+c.nodeID+" - Files:");
							/*for(DocNode d: c.getRepPoints()){
								d.setClusterID(c.nodeID);
								System.out.print(((DocNode)d).fileName+" ");

							}*/
							System.out.println(c.files);
							if(c.getChildren().size()>1){
								System.out.println("\n Children:");
								for(Node n: c.getChildren()){
									System.out.print(n.nodeID+" ");
								}
							}
						}
						System.out.println("Number of clusters formed : "+hc.clustersAtThisLevel.size());
						//or check if clusters don't change between two levels?
						if(hc.clustersAtThisLevel.size()<=k || hc.clustersAtThisLevel.size()==clustersInPreviousLevel){
							hc.flagToStop[0]=true;
						}
					}
					MPI.COMM_WORLD.Bcast(hc.flagToStop, 0, 1, MPI.BOOLEAN, 0);
					if(hc.flagToStop[0])
						break;
					iteration++;
				}
				
				if(MPI.COMM_WORLD.Rank()==0){
					long endTime = System.currentTimeMillis();
					System.out.println("Time Taken: "+(endTime-startTime));
					Cluster root = hc.mergeAllCluster();
					new TreeView(root).setVisible(true);
					
					//Print Result to File
					PrintWriter out = new PrintWriter(new File("output.txt"));
					generateOutputFile(out, root, 0);
					out.close();
				}
				MPI.Finalize();
				
	}
	
	//Generate Output File
	public static void generateOutputFile(PrintWriter out, Cluster root, int indent){
		for(int i=0;i<indent;i++){
			out.print("\t");
		}
		out.println(root.files.toString());
		if(root.getChildren() != null && root.getChildren().size()>0){
			for(Node child : root.getChildren()){
				generateOutputFile(out, (Cluster) child, indent+1);
			}
		}
		/*if(root.getChildren() != null && root.getChildren().size()>0){
			for(int i=0;i<indent;i++){
				out.print(" ");
			}
			out.println("Cluster "+ root.nodeID);
			for(Node child : root.getChildren()){
				generateOutputFile(out, (Cluster) child, indent+1);
			}
		}else{
			for(int i=0;i<indent;i++){
				out.print(" ");
			}
			out.print("Cluster "+ root.nodeID+" ");
			out.println(root.files.toString());
		}*/
	}
	//Testing Purpose Prints
	public static void printComponent(List<List<DocNode>> components){
		MPI.COMM_WORLD.Barrier();
		
		for(int i=0;i<MPI.COMM_WORLD.Size();i++){
			MPI.COMM_WORLD.Barrier();
			if(i==MPI.COMM_WORLD.Rank()){
				System.out.println("Processor : "+MPI.COMM_WORLD.Rank());
				int count = 1;
				for(List<DocNode> x: components){
					System.out.println("Cluster "+count);
					for(DocNode d: x){
						System.out.print(d.fileName+" ");
					}
					count++;
					System.out.println(" ");
				}
				System.out.println(" ");
			}
		}
	}
	public static void printCluster(List<Cluster> clusters){
		MPI.COMM_WORLD.Barrier();
		
		for(int i=0;i<MPI.COMM_WORLD.Size();i++){
			MPI.COMM_WORLD.Barrier();
			if(i==MPI.COMM_WORLD.Rank()){
				System.out.println("Processor : "+MPI.COMM_WORLD.Rank());
				for(Cluster c: clusters){
					System.out.println("\n Node(cluster) "+c.nodeID+" - representative points:");
					for(DocNode d: c.getRepPoints()){
						System.out.print(d.fileName+" ");
					}
				}
				System.out.println(" ");
			}
		}
	}
}
