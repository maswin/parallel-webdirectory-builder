package edu.tce.cse.clustering;

import java.io.Serializable;
import java.util.List;

public class LeafCluster extends Cluster implements Serializable{
	int processorID;
	int directoryID;
	public LeafCluster(long id, int pID, int dID, List<? extends Node> nodes, double percent){
		super(id, nodes, percent);
		this.processorID = pID;
		this.directoryID = dID;
	}
}
