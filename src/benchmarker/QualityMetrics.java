package benchmarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QualityMetrics {
	public static void main(String args[]){
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Input Folder Name & Output File Name");
		qualityMeasure(sc.nextLine(),sc.nextLine());		
	}
	public static void qualityMeasure(String inputFolder, String outputFile){
		//Get Data
				processIOData po = new processIOData();
				MinCluster actual = po.processInputData(inputFolder);
				MinCluster calculated = po.processOutputData(outputFile);
				List<MinCluster> hActual = po.getAllClusters(actual);
				List<MinCluster> hCalculated = po.getAllClusters(calculated);
				//Level 1 Alone
				List<MinCluster> oActual = po.getLevelCluster(actual, 1);
				List<MinCluster> oCalculated = po.getLevelCluster(calculated, 1);

				//Metrics
				FMeasure fm = new FMeasure();
				ClusterPurity cp = new ClusterPurity();
				RandIndex ri = new RandIndex();
				
				System.out.println("All vs All");
				//F-Measure
				System.out.println("F-Measure :");
				System.out.println(fm.FMeasure(hActual, hCalculated));
				
				//Cluster Purity
				System.out.println("Cluster Purity :");
				System.out.println(cp.ClusterPurity(hActual, hCalculated));
				
				//Rand Index
				System.out.println("Rand Index :");
				System.out.println("RI = "+ri.findRandIndex(hActual, hCalculated));
				
				System.out.println("All vs First Level");
				//F-Measure
				System.out.println("F-Measure :");
				System.out.println(fm.FMeasure(hActual, oCalculated));
				
				//Cluster Purity
				System.out.println("Cluster Purity :");
				System.out.println(cp.ClusterPurity(hActual, oCalculated));
				
				//Rand Index
				System.out.println("Rand Index :");
				System.out.println("RI = "+ri.findRandIndex(hActual, oCalculated));
				
				System.out.println("First vs First Level");
				//F-Measure
				System.out.println("F-Measure :");
				System.out.println(fm.FMeasure(oActual, oCalculated));
				
				//Cluster Purity
				System.out.println("Cluster Purity :");
				System.out.println(cp.ClusterPurity(oActual, oCalculated));
				
				//Rand Index
				System.out.println("Rand Index :");
				System.out.println("RI = "+ri.findRandIndex(oActual, oCalculated));
	}
}
