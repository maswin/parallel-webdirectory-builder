package benchmarker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FMeasure {

	public double recall(MinCluster actual, MinCluster calculated){
		double recall = 0d;
		
		Set<String> intersection;
		intersection = new HashSet<String>(actual.files);
		intersection.retainAll(calculated.files);
		
		recall = (double)(intersection.size()/(actual.files.size()*1.0));
		
		return recall;
	}
	
	public double precision(MinCluster actual, MinCluster calculated){
		double precision = 0d;
		
		Set<String> intersection;
		intersection = new HashSet<String>(actual.files);
		intersection.retainAll(calculated.files);
		
		precision = (double)(intersection.size()/(calculated.files.size()*1.0));
		
		return precision;
	}
	public double FMeasure(List<MinCluster> actual, List<MinCluster> calculated){
		double fMeasure = 0d;
		
		int[][] confusionMatrix = new int[calculated.size()][actual.size()];
		int[] maxMatch = new int[calculated.size()];
		
		int max;
		double maxValue;
		Set<String> intersection;
		for(int i=0;i<calculated.size();i++){
			max = 0;
			maxValue = Double.MIN_VALUE;
			for(int j=0;j<actual.size();j++){
				intersection = new HashSet<String>(calculated.get(i).files);
				intersection.retainAll(actual.get(j).files);
				confusionMatrix[i][j] = intersection.size();
				if(confusionMatrix[i][j] > maxValue){
					max = j;
					maxValue = confusionMatrix[i][j];
				}else if(confusionMatrix[i][j] == maxValue){
					if(actual.get(j).files.size() < actual.get(max).files.size()){
						max = j;
					}
				}
			}
			maxMatch[i] = max;
		}
		
		//debug(confusionMatrix);
		double totalPrecision = 0d;
		double totalRecall = 0d;
		
		for(int index = 0; index<calculated.size();index++){
			totalPrecision += precision(actual.get(maxMatch[index]),calculated.get(index));
			totalRecall += recall(actual.get(maxMatch[index]),calculated.get(index));
		}
		
		totalPrecision = (totalPrecision/(calculated.size()*1.0));
		totalRecall = (totalRecall/(calculated.size()*1.0));
		
		if(totalPrecision!= 0 && totalRecall != 0)
			fMeasure = (double)(2*((totalPrecision*totalRecall)/((totalPrecision+totalRecall)*1.0)));
		return fMeasure;
	}
	
	public void FMeasureByLevel(List<List<MinCluster>> actual, List<List<MinCluster>> calculated){
		
		double fMeasure = 0d;
		double levelFMeasure = 0d;
		int count = 0;
		for(int i=0;i<actual.size() && i<calculated.size(); i++){
			levelFMeasure = FMeasure(actual.get(i),calculated.get(i));
			System.out.println("Level "+(i+1)+" : "+levelFMeasure);
			fMeasure += levelFMeasure;
			count++;
		}
		fMeasure = (double)(fMeasure/(count*1.0));
		System.out.println("F-Measure : "+fMeasure);
	}
	void debug(Object... o){
		System.out.println(Arrays.deepToString(o));
	}
	
	/*
	public static void main(String args[]){
		
		processIOData po = new processIOData();
		MinCluster actual = po.processInputData("TestDocuments");
		MinCluster calculated = po.processOutputData("output.txt");
		List<List<MinCluster>> hActual = po.getLevelsOfClusters(actual);
		List<List<MinCluster>> hCalculated = po.getLevelsOfClusters(calculated);
		
		FMeasure fm = new FMeasure();
		fm.FMeasure(hActual, hCalculated);
		
		
	}*/
}
