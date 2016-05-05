package edu.tce.cse.model;

import edu.tce.cse.document.DocNode;

public class RepPointData {
	public DocNode d;
	public float distanceFromCentroid;
	public RepPointData(DocNode d, float dist){
		this.d = d;
		this.distanceFromCentroid = dist;
	}
	
}
