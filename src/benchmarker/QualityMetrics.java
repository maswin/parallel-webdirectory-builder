package benchmarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QualityMetrics {
	public static void main(String args[]){
		
		Scanner sc = new Scanner(System.in);
		//Get Data
		processIOData po = new processIOData();
		MinCluster actual = po.processInputData("TestDocuments");
		System.out.println("Enter Output File Name");
		MinCluster calculated = po.processOutputData(sc.nextLine());
		List<MinCluster> hActual = po.getAllClusters(actual);
		List<MinCluster> hCalculated = po.getAllClusters(calculated);
		
		//F-Measure
		System.out.println("F-Measure :");
		FMeasure fm = new FMeasure();
		System.out.println(fm.FMeasure(hActual, hCalculated));
		
		//Cluster Purity
		System.out.println("Cluster Purity :");
		ClusterPurity cp = new ClusterPurity();
		System.out.println(cp.ClusterPurity(hActual, hCalculated));
		
		//Rand Index
		System.out.println("Rand Index :");
		RandIndex ri = new RandIndex();
		System.out.println("RI = "+ri.findRandIndex(hActual, hCalculated));
		
		
	}
}
