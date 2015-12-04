package edu.tce.cse.clustering;

import java.io.Serializable;
import java.util.List;

public class LeafCluster extends Cluster implements Serializable{
	int processorID;
	int directoryID;
	//boolean isChildDocNode;
	public LeafCluster(long id, int pID, int dID, List<Long> nodes, double percent, boolean isDocNode){
		super(id, nodes, percent, isDocNode);
		this.processorID = pID;
		this.directoryID = dID;
	}
}
