package edu.tce.cse.webdirectorybuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import benchmarker.QualityMetrics;
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
	public static String inputFolder = "TestDocuments"; // -i
	public static String outputFile = "output.txt"; // -o

	public static int numOfCluster = 5; //-n
	public static double repPointPercent = 50.0; //-r
	public static double initK = 5; //-k
	public static double initL = 10; //-l
	public static double kRatio = 0.0; //-kr
	public static double lRatio = 0.0; //-lr

	public static boolean gui = true;

	public static void errorReport(boolean help){
		if(MPI.COMM_WORLD.Rank()==0){
			if(!help)
				System.err.println("Invalid Option");
			System.err.println("Usage : ");
			System.err.println("--------------------------------------------------------------");
			System.err.println("-i or Input file");
			System.err.println("-o or Output Folder");
			System.err.println("-h or --help Help");
			System.err.println("-n (Integer value) Number of clusters");
			System.err.println("-r (Double value) Percentage of representative points");
			System.err.println("-k (Integer value) Signatue Length");
			System.err.println("-l (Integer value) Number of hash functions");
			System.err.println("-kr (Double value) Signature length increasing ratio");
			System.err.println("-lr (Integer value) Number of hash functions decreasing ratio");
			System.err.println("--------------------------------------------------------------");
		}
	}
	public static boolean validateInput(String args[]){
		try{
			//Starts with -
			String pattern = "^-[a-zA-Z]*$";

			System.out.println("Args "+"<"+MPI.COMM_WORLD.Rank()+"> "+Arrays.deepToString(args));
			for(int i=0;i<args.length;i++){
				String word = args[i];
				if(word.matches(pattern)){
					switch(word){
					case "-i": inputFolder = args[i+1];
					break;
					case "-o": outputFile = args[i+1];
					break;
					case "-n": int nVal = Integer.parseInt(args[i+1]);
					numOfCluster = nVal;
					break;
					case "-r": double rVal = Double.parseDouble(args[i+1]);
					repPointPercent = rVal;
					break;
					case "-k": int kVal = Integer.parseInt(args[i+1]);
					initK = kVal;
					break;
					case "-l": int lVal = Integer.parseInt(args[i+1]);
					initL = lVal;
					break;
					case "-kr": double krVal = Double.parseDouble(args[i+1]);
					kRatio = krVal;
					break;
					case "-lr": double lrVal = Double.parseDouble(args[i+1]);
					lRatio = lrVal;
					break;
					case "-h": errorReport(true);
					return false;
					case "--help": errorReport(true);
					return false;
					default: errorReport(false);
					return false;
					}
				}else if(word.equals("nogui")){
					gui = false;
				}
			}
		}catch(Exception e){
			errorReport(false);
			return false;
		}
		return true;
	}
	public static void main(String args[]) throws FileNotFoundException{

		MPI.Init(args);
		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();

		if(!validateInput(args)){
			System.out.println("Exiting <"+MPI.COMM_WORLD.Rank()+">");
			return;
		} 
		if(MPI.COMM_WORLD.Rank() == 0){
			System.out.println("Parameters");
			System.out.println("Number of Clusters : "+numOfCluster);
			System.out.println("Percentage of Representative Points : "+repPointPercent);
			System.out.println("Signatue Length : "+initK);
			System.out.println("Number of hash functions : "+initL);
			System.out.println("Signature length increasing ratio : "+kRatio);
			System.out.println("Number of hash functions decreasing ratio : "+lRatio);
		}
		//Time Calc
		long startTimeData = System.currentTimeMillis();

		//gather Clusters (initial) from all processors
		Directory directory = new Directory();
		System.out.println("Started Id : "+id+"/"+size);
		HierarchicalClustering hc = new HierarchicalClustering(inputFolder);

		List<DocNode> nodeList = hc.preprocess();
		System.out.println("Processor "+MPI.COMM_WORLD.Rank()+" ---Data Received---");
		//Time Calc
		long startTimeExec = System.currentTimeMillis();

		DistributedLSH dLSH = new DistributedLSH(nodeList.get(0).tfIdf.length, initK, initL, kRatio, lRatio);
		hc.clustersAtThisLevel = hc.initialClustering(nodeList, directory, repPointPercent);

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
				if(hc.clustersAtThisLevel.size()<=numOfCluster || hc.clustersAtThisLevel.size()==clustersInPreviousLevel){
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
			System.out.println("Data Processing + Execution Time: "+(endTime-startTimeData));
			System.out.println("Execution Time: "+(endTime-startTimeExec));

			Cluster root = hc.mergeAllCluster();
			System.out.println("Number of Files ("+inputFolder+"): "+nodeList.size());
			System.out.println("Dimension "+root.getRepPoints().get(0).getTfIdf().length);
			if(gui){
				//GUI
				new TreeView(root).setVisible(true);
			} 

			//Print Result to File
			PrintWriter out = new PrintWriter(new File(outputFile));
			generateOutputFile(out, root, 0);
			out.close();
			QualityMetrics.qualityMeasure(inputFolder,outputFile);
		}
		MPI.Finalize();

	}

	//Generate Output File
	public static void generateOutputFile(PrintWriter out, Cluster root, int indent){
		for(int i=0;i<indent;i++){
			out.print(" ");
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
