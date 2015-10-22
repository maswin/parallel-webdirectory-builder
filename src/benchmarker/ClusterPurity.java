package benchmarker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClusterPurity {

	public double ClusterPurityByLevel(List<MinCluster> actual, List<MinCluster> calculated){
		double purity = 0d;
		
		int[][] confusionMatrix = new int[calculated.size()][actual.size()];
		
		int max;
		int totalMax = 0;
		Set<String> intersection;
		for(int i=0;i<calculated.size();i++){
			max = 0;
			for(int j=0;j<actual.size();j++){
				intersection = new HashSet<String>(calculated.get(i).files);
				intersection.retainAll(actual.get(j).files);
				confusionMatrix[i][j] = intersection.size();
				if(confusionMatrix[i][j] > max){
					max = confusionMatrix[i][j];
				}
			}
			totalMax += max;
		}
		
		int totalSize = 0;
		for(int i=0;i<calculated.size();i++){
			totalSize += calculated.get(i).files.size();
		}
		purity = (double)(totalMax/(totalSize*1.0));
		
		return purity;
	}
	
	public void ClusterPurity(List<List<MinCluster>> actual, List<List<MinCluster>> calculated){
		
		double purity = 0d;
		double levelPurity= 0d;
		int count = 0;
		for(int i=0;i<actual.size() && i<calculated.size(); i++){
			levelPurity = ClusterPurityByLevel(actual.get(i),calculated.get(i));
			System.out.println("Level "+(i+1)+" : "+levelPurity);
			purity += levelPurity;
			count++;
		}
		purity = (double)(purity/(count*1.0));
		System.out.println("Cluster Purity : "+purity);
	}
}
