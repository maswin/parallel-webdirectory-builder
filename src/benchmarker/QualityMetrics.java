package benchmarker;

import java.util.List;

public class QualityMetrics {
	public static void main(String args[]){
		
		//Get Data
		processIOData po = new processIOData();
		MinCluster actual = po.processInputData("TestDocuments");
		MinCluster calculated = po.processOutputData("output.txt");
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
		
	}
}
