package edu.tce.cse.clustering;

import java.io.Serializable;

public class LeafCluster extends Cluster implements Serializable{
	int processorID;
	int directoryID;
	public LeafCluster(long id, int pID, int dID){
		super(id);
		this.processorID = pID;
		this.directoryID = dID;
	}
}
