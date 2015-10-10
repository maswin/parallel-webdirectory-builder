package edu.tce.cse.model;

import java.io.Serializable;

public class Centroid implements Serializable{
	public long clusterId;
	public double tfIdf[];
	public Centroid(long clusterId, double tfIdf[]){
		this.clusterId = clusterId;
		this.tfIdf = tfIdf;
	}
}
