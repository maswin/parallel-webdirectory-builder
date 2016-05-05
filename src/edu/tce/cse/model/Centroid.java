package edu.tce.cse.model;

import java.io.Serializable;

import cern.colt.matrix.tdouble.DoubleMatrix1D;

public class Centroid implements Serializable{
	public long clusterId;
	public DoubleMatrix1D tfIdf;
	public Centroid(long clusterId, DoubleMatrix1D tfIdf){
		this.clusterId = clusterId;
		this.tfIdf = tfIdf;
	}
}
