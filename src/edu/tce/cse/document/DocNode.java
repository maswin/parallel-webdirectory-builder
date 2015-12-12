package edu.tce.cse.document;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;
import edu.tce.cse.clustering.Edge;
import edu.tce.cse.clustering.Node;
import edu.tce.cse.model.Centroid;
import edu.tce.cse.model.PartialBetweenness;
import edu.tce.cse.util.SuperBit;


public class DocNode extends Node implements Comparable<DocNode>, Serializable{
	
	public String fileName;
	public SparseDoubleMatrix1D tfIdf;
	private SparseDoubleMatrix1D reducedTfIdf;//Only used within the processor
	
	private BitSet signtature;
	
	public float centrality;
	public PartialBetweenness container;
	public long clusterID;
	
	public DocNode(long id, String fileName, SparseDoubleMatrix1D tfIdf){
		super(id);
		this.tfIdf = tfIdf;
		this.reducedTfIdf = null;
		this.fileName = fileName;
		this.container = new PartialBetweenness();	
	}
	
	public BitSet getSigntature() {
		return signtature;
	}

	public void setSigntature(BitSet signtature) {
		this.signtature = signtature;
	}

	//Getter & Setter
	public long getClusterID() {
		return clusterID;
	}
	public void setClusterID(long clusterID) {
		this.clusterID = clusterID;
	}
	
	public SparseDoubleMatrix1D getTfIdf() {
		return tfIdf;
	}
	public void setTfIdf(SparseDoubleMatrix1D tfIdf) {
		this.tfIdf = tfIdf;
	}
	public SparseDoubleMatrix1D getReducedTfIdf() {
		//return tfIdf;
		return reducedTfIdf;
	}
	public void setReducedTfIdf(SparseDoubleMatrix1D tfIdf) {
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

        Algebra algebra = new Algebra();

        float sim = (float) (vector1.zDotProduct(vector2) / 
                (Math.sqrt(algebra.norm2(vector1))*Math.sqrt(algebra.norm2(vector2))));
        if(Float.isNaN(sim)){
        	return 0f;
        }
        return sim;
	}
	public float findCosDistance(DocNode d){
		return (1-findCosSimilarity(d));
	}
	
	
	public float findCosSimilarity(Centroid d){

		DoubleMatrix1D vector1 = this.getTfIdf();
		DoubleMatrix1D vector2 = new SparseDoubleMatrix1D(d.tfIdf);

        Algebra algebra = new Algebra();

        float sim = (float) (vector1.zDotProduct(vector2) / 
                (Math.sqrt(algebra.norm2(vector1))*Math.sqrt(algebra.norm2(vector2))));;
        if(Float.isNaN(sim)){
        	return 0f;
        }
        return sim;
	}
	public float findCosDistance(Centroid d){
		return (1-findCosSimilarity(d));
	}
	//Distance using reduced Tf-Idf
	/*public float findReducedCosSimilarity(DocNode d){
		//return findCosSimilarity(d);
		DoubleMatrix1D vector1 = this.getReducedTfIdf();
		DoubleMatrix1D vector2 = d.getReducedTfIdf();

        Algebra algebra = new Algebra();

        float sim = (float) (vector1.zDotProduct(vector2) / 
                (Math.sqrt(algebra.norm2(vector1))*Math.sqrt(algebra.norm2(vector2))));
        if(Float.isNaN(sim)){
        	return 0f;
        }
        return sim;
	}
	public float findReducedCosDistance(DocNode d){
		return (1-findReducedCosSimilarity(d));
	}*/
	public float findApproxCosDistance(DocNode d){
		BitSet temp = (BitSet) this.signtature.clone();
		temp.xor(d.signtature);
		return ((float)temp.cardinality()/temp.size());
	}
	
	public float findEuclideanSimilarity(DocNode d){
		float E = 0.0f;
		for(int i=0; i<tfIdf.toArray().length; i++){
			E += Math.pow((tfIdf.toArray()[i]-d.tfIdf.toArray()[i]), 2);
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
