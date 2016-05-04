package edu.tce.cse.webdirectorybuilder;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Node;
import edu.tce.cse.document.DocNode;
import mpi.MPI;

public class HelperUtil {

	private WebDirectoryBuilder wbd;

	public HelperUtil(WebDirectoryBuilder wbd) {
		this.wbd = wbd;
	}

	public void errorReport(boolean isHelp){
		if(MPI.COMM_WORLD.Rank()==0){
			if(!isHelp)
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

	public boolean validateInput(String args[]){
		try{
			//Starts with -
			String pattern = "^-[a-zA-Z]*$";

			System.out.println("Args "+"<"+MPI.COMM_WORLD.Rank()+"> "+Arrays.deepToString(args));
			for(int i=0;i<args.length;i++){
				String word = args[i];
				if(word.matches(pattern)){
					switch(word){
					case "-i": wbd.inputFolder = args[i+1];
					break;
					case "-o": wbd.outputFile = args[i+1];
					break;
					case "-n": int nVal = Integer.parseInt(args[i+1]);
					wbd.numOfCluster = nVal;
					break;
					case "-r": double rVal = Double.parseDouble(args[i+1]);
					wbd.repPointPercent = rVal;
					break;
					case "-k": int kVal = Integer.parseInt(args[i+1]);
					wbd.initK = kVal;
					break;
					case "-l": int lVal = Integer.parseInt(args[i+1]);
					wbd.initL = lVal;
					break;
					case "-kr": double krVal = Double.parseDouble(args[i+1]);
					wbd.kRatio = krVal;
					break;
					case "-lr": double lrVal = Double.parseDouble(args[i+1]);
					wbd.lRatio = lrVal;
					break;
					case "-h": errorReport(true);
					return false;
					case "--help": errorReport(true);
					return false;
					default: errorReport(false);
					return false;
					}
				}else if(word.equals("nogui")){
					wbd.gui = false;
				}
			}
		}catch(Exception e){
			errorReport(false);
			return false;
		}
		return true;
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
