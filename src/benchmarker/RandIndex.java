package benchmarker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.MathUtils;

public class RandIndex {
	int[][] confusionMatrix;
	int[] sumOverColumns;
	int[] sumPerColumn;
	int totalSum;
	double tp;
	public double findRandIndex(List<MinCluster> hActual, List<MinCluster> hCalculated){
		double tpPlusFp = 0, tpPlusFn = 0, fp, tn, fn;
		buildConfusionMatrix(hActual, hCalculated);
		for(int i=0; i<sumPerColumn.length; i++){
			tpPlusFp+= findBinomialCoefficient(sumPerColumn[i],2);
		}
		for(int i=0; i<sumOverColumns.length; i++){
			tpPlusFn+= findBinomialCoefficient(sumOverColumns[i],2);
		}
		
		fp = tpPlusFp - tp;
		fn = tpPlusFn - tp;
	    tn = findBinomialCoefficient(totalSum, 2) - tp - fp - fn;
	    double randIndex = (tp + tn) / (tp + fp + fn + tn);
	    return randIndex;
	}
	public void buildConfusionMatrix(List<MinCluster> actual, List<MinCluster> calculated){
		confusionMatrix = new int[actual.size()][calculated.size()];
		sumOverColumns = new int[actual.size()];
		sumPerColumn = new int[calculated.size()];
		totalSum = 0;
		Set<String> intersection;
		for(int i=0;i<actual.size();i++){
			int sum = 0;
			for(int j=0;j<calculated.size();j++){
				intersection = new HashSet<String>(actual.get(i).files);
				intersection.retainAll(calculated.get(j).files);
				confusionMatrix[i][j] = intersection.size();
				sum+=confusionMatrix[i][j];
				tp+= findBinomialCoefficient(confusionMatrix[i][j], 2);
			}
			sumOverColumns[i]=sum;
		}
		for(int j=0; j<calculated.size(); j++){
			int sum=0;
			for(int i=0; i<actual.size(); i++){
				sum+=confusionMatrix[i][j];
			}
			sumPerColumn[j]=sum;
			totalSum += sum;
		}
	}
	public double findBinomialCoefficient(int N, int k){
		if(k>N)
			return 0;
		return CombinatoricsUtils.binomialCoefficient(N,k);
	}
}
