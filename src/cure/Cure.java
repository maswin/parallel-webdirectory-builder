package cure;

import java.io.*;
import java.util.*;

import benchmarker.QualityMetrics;
import edu.tce.cse.document.DocNode;
import edu.tce.cse.example.sampleData;

/**
 * CURE Clustering Algorithm
 * The algorithm follows the six steps as specified in the original paperwork by Guha et. al.
 * 
 * @version 1.0
 * @author jmishra
 */
public class Cure {

	public static String inputFolder = "TestDocuments";
	public static String outputFile = "CureOutput.txt";

	/** The Input Parameters to the algorithm **/
	private int totalNumberOfPoints;
	private int numberOfClusters;
	private int minRepresentativeCount;
	private double shrinkFactor;
	private double requiredRepresentationProbablity;
	private int numberOfPartitions;
	private int reducingFactorForEachPartition;

	//Added by Aswin
	int tfIdfSize = 0;

	private Point[] dataPoints;
	private ArrayList outliers;
	private HashMap dataPointsMap;

	private static int currentRepAdditionCount;
	private Hashtable integerTable;


	public Cure(String[] args) throws IOException {

		System.out.println("CURE Clustering Algorithm");
		System.out.println("-------------------------\n");

		//Time Calc
		long startTimeData = System.currentTimeMillis();

		sampleData s = new sampleData(inputFolder);
		initializeParameters(args, s);
		readDataPoints(s);
		System.out.println("Data Received");

		//Time Calc
		long startTimeExec = System.currentTimeMillis();

		int sampleSize = calculateSampleSize();
		ArrayList randomPointSet = selectRandomPoints(sampleSize);
		System.out.println("Random Points selected");
		ArrayList[] partitionedPointSet = partitionPointSet(randomPointSet);
		System.out.println("Partitioned");
		ArrayList subClusters = clusterSubPartitions(partitionedPointSet);
		System.out.println("Sub Clusters :");
		displayClusters(subClusters);
		if(reducingFactorForEachPartition >= 10) {
			eliminateOutliersFirstStage(subClusters, 1);
		}
		else {
			eliminateOutliersFirstStage(subClusters, 0);
		}
		ArrayList clusters = clusterAll(subClusters);
		clusters = labelRemainingDataPoints(clusters);



		System.out.println("Final Clusters :");
		displayClusters(clusters);

		long endTime = System.currentTimeMillis();
		System.out.println("Data Processing + Execution Time: "+(endTime-startTimeData));
		System.out.println("Execution Time: "+(endTime-startTimeExec));
		showClusters(clusters);
		QualityMetrics.qualityMeasure(inputFolder,outputFile);
	}


	/**
	 * Initializes the Parameters
	 * @param args The Command Line Argument
	 * @throws IOException 
	 */
	private void initializeParameters(String[] args, sampleData s) throws IOException {
		if(args.length == 0) {
			//dataFile = "data.txt";
			totalNumberOfPoints = s.getSampleDocNodes().size();
			numberOfClusters = 6;
			minRepresentativeCount = 6;
			shrinkFactor = 0.5;
			requiredRepresentationProbablity = 0.1;
			numberOfPartitions = 2;
			reducingFactorForEachPartition = 2;
		}
		else {
			//dataFile = args[1];
			totalNumberOfPoints = s.getSampleDocNodes().size();
			numberOfClusters = 6;
			minRepresentativeCount = 6;
			shrinkFactor = 0.5;
			requiredRepresentationProbablity = 0.1;
			numberOfPartitions = 2;
			reducingFactorForEachPartition = 2;
			try{
				for(int i=0;i<args.length;i++){
					String word = args[i];
					String pattern = "^-[a-zA-Z]*$";
					if(word.matches(pattern)){
						switch(word){
						case "-i": inputFolder = args[i+1];
						break;
						case "-o": outputFile = args[i+1];
						break;
						case "-n": int nVal = Integer.parseInt(args[i+1]);
						numberOfClusters = nVal;
						break;
						default: break;
						}
					}
				}
			} catch (Exception e){
				System.out.println("Invalid Input");
			}
			System.out.println("Number of Clusters Required : "+numberOfClusters);
			/*totalNumberOfPoints = Integer.parseInt(args[2]);
			numberOfClusters = Integer.parseInt(args[3]);
			minRepresentativeCount = Integer.parseInt(args[4]);
			shrinkFactor = Double.parseDouble(args[5]);
			requiredRepresentationProbablity = Double.parseDouble(args[6]);
			numberOfPartitions = Integer.parseInt(args[7]);
			reducingFactorForEachPartition = Integer.parseInt(args[8]);*/
		}
		tfIdfSize = s.getSampleDocNodes().get(0).tfIdf.length;
		dataPoints = new Point[totalNumberOfPoints];
		dataPointsMap = new HashMap();
		currentRepAdditionCount = totalNumberOfPoints;
		integerTable = new Hashtable();
		outliers = new ArrayList();
	}

	/**
	 * Reads the data points from file
	 */
	private void readDataPoints(sampleData s) {
		int pointIndex = 0;
		FileReader fr = null;
		try {
			List<DocNode> docNodes = s.getSampleDocNodes();
			for(DocNode d : docNodes){
				dataPoints[pointIndex] = new Point(d.tfIdf,pointIndex,d.fileName);
				dataPointsMap.put(pointIndex, dataPoints[pointIndex]);
				pointIndex++;
			}

		} catch(Exception e){
			System.out.println("Data point Reading Error");
			debug(e);
		}
	}

	/**
	 * Calculates the Sample Size based on Chernoff Bounds Mentioned in the CURE Algorithm
	 * @return
	 * int The Sample Data Size to be worked on
	 */
	private int calculateSampleSize() {
		return (int)((0.5 * totalNumberOfPoints) + (numberOfClusters * Math.log10(1/requiredRepresentationProbablity)) + (numberOfClusters * Math.sqrt(Math.pow(Math.log10(1/requiredRepresentationProbablity), 2) + (totalNumberOfPoints/numberOfClusters) * Math.log10(1/requiredRepresentationProbablity))));
	}

	/**
	 * Select random points from the data set
	 * @param sampleSize The sample size selected
	 * @return
	 * ArrayList The Selected Random Points
	 */
	private ArrayList selectRandomPoints(int sampleSize) {
		ArrayList randomPointSet = new ArrayList();
		Random random = new Random();
		for(int i=0; i<sampleSize && i < totalNumberOfPoints; i++) {
			int index = random.nextInt(totalNumberOfPoints);
			if(integerTable.containsKey(index)) {
				i--; continue;
			}
			else {
				Point point = dataPoints[index];
				randomPointSet.add(point);
				integerTable.put(index, "");
			}
		}
		return randomPointSet;
	}

	/**
	 * Partition the sampled data points to p partitions (p = numberOfPartitions)
	 * @param pointSet Sample data point set
	 * @return
	 * ArrayList[] Data Set Partitioned Sets
	 */
	private ArrayList[] partitionPointSet(ArrayList pointSet) {
		ArrayList partitionedSet[] = new ArrayList[numberOfPartitions];
		Iterator iter = pointSet.iterator();
		for(int i = 0; i < numberOfPartitions - 1 ; i++) {
			partitionedSet[i] = new ArrayList();
			int pointIndex = 0;
			while(pointIndex < pointSet.size() / numberOfPartitions) {
				partitionedSet[i].add(iter.next());
				pointIndex++;
			}
		}
		partitionedSet[numberOfPartitions - 1] = new ArrayList();
		while(iter.hasNext()) {
			partitionedSet[numberOfPartitions - 1].add(iter.next());
		}
		return partitionedSet;
	}

	/**
	 * Cluster each partitioned set to n/pq clusters
	 * @param partitionedSet Data Point Set
	 * @return
	 * ArrayList Clusters formed
	 */
	private ArrayList clusterSubPartitions(ArrayList partitionedSet[]) {
		ArrayList clusters = new ArrayList();
		int numberOfClusterInEachPartition = totalNumberOfPoints / (numberOfPartitions * reducingFactorForEachPartition);
		for(int i=0 ; i<partitionedSet.length; i++) {
			ClusterSet clusterSet = new ClusterSet(partitionedSet[i],numberOfClusterInEachPartition, minRepresentativeCount, shrinkFactor, dataPointsMap);
			Cluster[] subClusters = clusterSet.getAllClusters();
			for(int j=0; j<subClusters.length; j++) {
				clusters.add(subClusters[j]);
			}
		}
		return clusters;
	}

	/**
	 * Eliminates outliers after pre-clustering
	 * @param clusters Clusters present
	 * @param outlierEligibilityCount Min Threshold count for not being outlier cluster
	 */
	private void eliminateOutliersFirstStage(ArrayList clusters, int outlierEligibilityCount) {
		Iterator iter = clusters.iterator();
		ArrayList clustersForRemoval = new ArrayList(); 
		while(iter.hasNext()) {
			Cluster cluster = (Cluster) iter.next();
			if(cluster.getClusterSize() <= outlierEligibilityCount) {
				updateOutlierSet(cluster);
				clustersForRemoval.add(cluster);
			}
		}
		while(!clustersForRemoval.isEmpty()) {
			Cluster c = (Cluster)clustersForRemoval.remove(0);
			clusters.remove(c);
		}
	}

	/**
	 * Cluster all remaining clusters. Merge all clusters using CURE's hierarchical clustering algorithm till specified number of clusters
	 * remain.
	 * @param clusters Pre-clusters formed
	 * @return
	 * ArrayList Set of clusters
	 */
	private ArrayList clusterAll(ArrayList clusters) {
		ClusterSet clusterSet = new ClusterSet(clusters, numberOfClusters, minRepresentativeCount, shrinkFactor, dataPointsMap, true, tfIdfSize);
		return clusterSet.mergeClusters();
	}

	/**
	 * Assign all remaining data points which were not part of the sampled data set to set of clusters formed
	 * @param clusters Set of clusters
	 * @return
	 * ArrayList Modified clusters
	 */
	private ArrayList labelRemainingDataPoints(ArrayList clusters) {

		for(int index = 0; index < dataPoints.length; index++) {
			if(integerTable.containsKey(index)) continue;
			Point p = dataPoints[index];
			double smallestDistance = 1000000;
			int nearestClusterIndex = -1;
			for(int i = 0; i < clusters.size(); i++) {
				ArrayList rep = ((Cluster)clusters.get(i)).rep;
				for(int j=0; j<rep.size(); j++) {
					double distance = p.calcDistanceFromPoint((Point)rep.get(j));
					if(distance < smallestDistance) {
						smallestDistance = distance;
						nearestClusterIndex = i;
					}
				}
			}
			if(nearestClusterIndex != -1) {
				((Cluster)clusters.get(nearestClusterIndex)).pointsInCluster.add(p);
			}
		}
		return clusters;
	}

	/**
	 * Update the outlier set for the clusters which have been identified as outliers
	 * @param cluster Outlier Cluster
	 */
	private void updateOutlierSet(Cluster cluster) {
		ArrayList outlierPoints = cluster.getPointsInCluster();
		Iterator iter = outlierPoints.iterator();
		while(iter.hasNext()) {
			outliers.add(iter.next());
		}
	}

	private void debug(Exception e) {
		e.printStackTrace(System.out);
	}

	/**
	 * Gets the current representative count so that the new points added do not conflict with older KD Tree indices 
	 * @return
	 * int Next representative count
	 */
	public static int getCurrentRepCount() {
		return ++currentRepAdditionCount;
	}

	public void showClusters(ArrayList clusters) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new File(outputFile));

		StringBuilder files = new StringBuilder("");

		for(int i=0; i<numberOfClusters; i++) {			
			Cluster cluster = (Cluster)clusters.get(i);
			for(int j=0; j<cluster.pointsInCluster.size(); j++) {
				Point p = (Point)cluster.pointsInCluster.get(j);
				files.append(p.fileName+";");
			}			
		}
		for(int j=0; j<outliers.size(); j++) {			
			Point p = (Point)outliers.get(j);
			files.append(p.fileName+";");
		}
		//System.out.println(files.toString());
		out.println(files.toString());		

		for(int i=0; i<numberOfClusters; i++) {	
			files = new StringBuilder(" ");
			Cluster cluster = (Cluster)clusters.get(i);
			for(int j=0; j<cluster.pointsInCluster.size(); j++) {
				Point p = (Point)cluster.pointsInCluster.get(j);
				files.append(p.fileName+";");
			}
			//System.out.println(files.toString());
			out.println(files.toString());
		}
		if(outliers.size()>0){
			files = new StringBuilder(" ");
			for(int j=0; j<outliers.size(); j++) {			
				Point p = (Point)outliers.get(j);
				files.append(p.fileName+";");
			}
			out.println(files.toString());
		}
		out.close();
		logOutlier();
		//logPlotScript(clusters.size());
	}

	public void displayClusters(ArrayList clusters){
		for(int i=0; i<numberOfClusters; i++) {
			Cluster cluster = (Cluster)clusters.get(i);
			System.out.println("Cluster "+(i+1));
			for(int j=0; j<cluster.pointsInCluster.size(); j++) {
				Point p = (Point)cluster.pointsInCluster.get(j);
				System.out.print(p.fileName + "\n");
			}
			System.out.println();
		}
	}
	private BufferedWriter getWriterHandle(String filename) {
		BufferedWriter out = null;
		try {
			FileWriter fw = new FileWriter(filename, true);
			out = new BufferedWriter(fw);
		} catch(Exception e) {
			debug(e);
		}
		return out;
	}

	private void closeWriterHandle(BufferedWriter out) {
		try {
			out.flush();
			out.close();
		} catch(Exception e) {
			debug(e);
		}
	}

	private void logCluster(Cluster cluster, String filename) {
		BufferedWriter out = getWriterHandle(filename);
		try {
			//out.write("#\tX\tY\n");
			for(int j=0; j<cluster.pointsInCluster.size(); j++) {
				Point p = (Point)cluster.pointsInCluster.get(j);
				out.write(p.fileName + "\n");
			}
		} catch(Exception e){
			debug(e);
		}
		closeWriterHandle(out);
	}

	private void logOutlier() {
		BufferedWriter out = getWriterHandle("outliers");
		try {
			//out.write("#\tX\tY\n");
			for(int j=0; j<outliers.size(); j++) {
				Point p = (Point)outliers.get(j);
				out.write(p.fileName + "\n");
			}
		} catch(Exception e){
			debug(e);
		}
		closeWriterHandle(out);
	}

	public static void main(String args[]) throws IOException{
		Cure c = new Cure(args);
	}
}