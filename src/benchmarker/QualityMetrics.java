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
				List<MinCluster> oCalculated = po.getLevelCluster(calculated, 1);

				//F-Measure
				System.out.println("F-Measure :");
				FMeasure fm = new FMeasure();
				System.out.println(fm.FMeasure(hActual, oCalculated));
				
				//Cluster Purity
				System.out.println("Cluster Purity :");
				ClusterPurity cp = new ClusterPurity();
				System.out.println(cp.ClusterPurity(hActual, oCalculated));
				
				//Rand Index
				System.out.println("Rand Index :");
				RandIndex ri = new RandIndex();
				System.out.println("RI = "+ri.findRandIndex(hActual, oCalculated));
	}
}
