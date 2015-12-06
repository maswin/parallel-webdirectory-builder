package edu.tce.cse.model;

import edu.tce.cse.document.DocNode;

public class RepPointData {
	public Long d;
	public float distanceFromCentroid;
	public RepPointData(Long d, float dist){
		this.d = d;
		this.distanceFromCentroid = dist;
	}
	
}
