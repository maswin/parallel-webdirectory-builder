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
	public String inputFolder = "TestDocuments"; // -i
	public String outputFile = "output.txt"; // -o

	public int numOfCluster = 5; //-n
	public double repPointPercent = 50.0; //-r
	public double initK = 5; //-k
	public double initL = 10; //-l
	public double kRatio = 0.0; //-kr
	public double lRatio = 0.0; //-lr

	public boolean gui = true;
	
	private void printParameters() {
		System.out.println("Parameters");
		System.out.println("Number of Clusters : "+numOfCluster);
		System.out.println("Percentage of Representative Points : "+repPointPercent);
		System.out.println("Signatue Length : "+initK);
		System.out.println("Number of hash functions : "+initL);
		System.out.println("Signature length increasing ratio : "+kRatio);
		System.out.println("Number of hash functions decreasing ratio : "+lRatio);
	}
	
	public void buildWebDirectory(int id, int size) throws FileNotFoundException { 
		if(MPI.COMM_WORLD.Rank() == 0) {
			printParameters();
		}
		//Time Calc
		long startTimeData = System.currentTimeMillis();

		//Gather Clusters (initial) from all processors
		System.out.println("Started Id : "+id+"/"+size);
		HierarchicalClustering hc = new HierarchicalClustering(inputFolder);

		List<DocNode> nodeList = hc.preprocess();
		System.out.println("Processor "+MPI.COMM_WORLD.Rank()+" ---Data Received---");
		
		//Time Calc
		long startTimeExec = System.currentTimeMillis();

		DistributedLSH dLSH = new DistributedLSH((int)nodeList.get(0).getTfIdf().size(), initK, initL, kRatio, lRatio);
		hc.clustersAtThisLevel = hc.initialClustering(nodeList, repPointPercent);

		int numberOfClustersInPreviousLevel = hc.clustersAtThisLevel.size();
		int startID = hc.clustersAtThisLevel.size();
		int iteration = 1;

		while(true){
			MPI.COMM_WORLD.Barrier();
			numberOfClustersInPreviousLevel = hc.clustersAtThisLevel.size();
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
				if(hc.clustersAtThisLevel.size()<=numOfCluster || hc.clustersAtThisLevel.size()==numberOfClustersInPreviousLevel){
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
			System.out.println("Dimension "+root.getRepPoints().get(0).getTfIdf().size());
			
			if(gui){
				//GUI
				new TreeView(root).setVisible(true);
			} 

			//Print Result to File
			PrintWriter out = new PrintWriter(new File(outputFile));
			HelperUtil.generateOutputFile(out, root, 0);
			out.close();
			QualityMetrics.qualityMeasure(inputFolder,outputFile);
		}	
	}
	
	public static void main(String args[]) throws FileNotFoundException{
		MPI.Init(args);
		int id = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		
		WebDirectoryBuilder wbd = new WebDirectoryBuilder();
		HelperUtil hu = new HelperUtil(wbd);
		
		if(!hu.validateInput(args)){
			System.out.println("Exiting <"+MPI.COMM_WORLD.Rank()+">");
			return;
		}
		
		wbd.buildWebDirectory(id, size);
		
		MPI.Finalize();
	}

}
