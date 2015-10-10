package edu.tce.cse.model;

import edu.tce.cse.document.DocNode;

public class Data{
	public Data(Centroid a, Centroid b){
		this.a = a.clusterId;
		this.b = b.clusterId;
	}
	public long a;
	public long b;
}