package benchmarker;

import java.util.ArrayList;
import java.util.List;

public class QualityMetrics {
	public static void main(String args[]){
		
		//Get Data
		processIOData po = new processIOData();
		MinCluster actual = po.processInputData("TestDocuments");
		MinCluster calculated = po.processOutputData("output.txt");
		List<List<MinCluster>> hActual = po.getLevelsOfClusters(actual);
		List<List<MinCluster>> hCalculated = po.getLevelsOfClusters(calculated);
		
		//F-Measure
		System.out.println("F-Measure :");
		FMeasure fm = new FMeasure();
		fm.FMeasure(hActual, hCalculated);
		
		//Cluster Purity
		System.out.println("Cluster Purity :");
		ClusterPurity cp = new ClusterPurity();
		cp.ClusterPurity(hActual, hCalculated);
		
		List<MinCluster> actualList=new ArrayList();
		List<MinCluster> calcList = new ArrayList();
		for(List<MinCluster> l: hActual)
			actualList.addAll(l);
		for(List<MinCluster> l: hCalculated)
			calcList.addAll(l);
		RandIndex ri = new RandIndex();
		System.out.println("RI = "+ri.findRandIndex(actualList, calcList));
		
		
	}
}
