package edu.tce.cse.document;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import edu.tce.cse.clustering.Edge;
import edu.tce.cse.clustering.Node;
import edu.tce.cse.model.PartialBetweenness;


public class DocNode extends Node implements Comparable<DocNode>, Serializable{
	
	public String fileName;
	private DoubleMatrix1D tfIdf = null;
	public transient DoubleMatrix1D reducedTfIdf;//Only used within the processor
	public float centrality;
	public transient PartialBetweenness container;
	public long clusterID;
	
	public DocNode(long id, String fileName, DoubleMatrix1D tfIdf){
		super(id);
		this.tfIdf = tfIdf;
		this.reducedTfIdf = null;
		this.fileName = fileName;
		this.container = new PartialBetweenness();
	}
	
	//Getter & Setter
	public long getClusterID() {
		return clusterID;
	}
	public void setClusterID(long clusterID) {
		this.clusterID = clusterID;
	}
	
	public DoubleMatrix1D getTfIdf() {
		return tfIdf;
	}
	public void setTfIdf(DoubleMatrix1D tfIdf) {
		this.tfIdf = tfIdf;
	}
	public DoubleMatrix1D getReducedTfIdf() {
		return reducedTfIdf;
	}
	public void setReducedTfIdf(DoubleMatrix1D tfIdf) {
		this.reducedTfIdf = tfIdf;
	}
	public float getCentrality(){
		return centrality;
	}
	public void setFileName(String name){
		this.fileName = name; 
	}
	
	public float findCosSimilarity(DocNode d){

		DoubleMatrix1D vector1 = this.getTfIdf();
        DoubleMatrix1D vector2 = d.getTfIdf();

        DenseDoubleAlgebra algebra = new DenseDoubleAlgebra();

        float sim = (float) (vector1.zDotProduct(vector2) / 
                (algebra.norm2(vector1)*algebra.norm2(vector2)));
        if(Float.isNaN(sim)){
        	return 0f;
        }
        return sim;
	}
	public float findCosDistance(DocNode d){
		return (1-findCosSimilarity(d));
	}
	
	//Distance using reduced Tf-Idf
	public float findReducedCosSimilarity(DocNode d){

		DoubleMatrix1D vector1 = this.getReducedTfIdf();
        DoubleMatrix1D vector2 = d.getReducedTfIdf();

        DenseDoubleAlgebra algebra = new DenseDoubleAlgebra();
        
        return (float) (vector1.zDotProduct(vector2) / 
                (algebra.norm2(vector1)*algebra.norm2(vector2)));
	}
	public float findReducedCosDistance(DocNode d){
		return (1-findReducedCosSimilarity(d));
	}
	
	public float findEuclideanSimilarity(DocNode d){
		float E = 0.0f;
		for(int i=0; i<tfIdf.size(); i++){
			E += Math.pow((tfIdf.get(i)-d.tfIdf.get(i)), 2);
		}
		return (float)(Math.abs(Math.sqrt(E)));
	}
	@Override
	public float findDistance(Node n) {
		// TODO Auto-generated method stub
		DocNode d = (DocNode)n;
		return findCosSimilarity(d);
	}
	@Override
	public int compareTo(DocNode o) {
		if (container.priority > o.container.priority) {
			return +1;
		} else {
			return -1;
		}
	}
}
