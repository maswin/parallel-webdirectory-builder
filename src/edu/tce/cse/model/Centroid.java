package edu.tce.cse.model;

public class Centroid {
	public long clusterId;
	public double tfIdf[];
	public Centroid(long clusterId, double tfIdf[]){
		this.clusterId = clusterId;
		this.tfIdf = tfIdf;
	}
}
