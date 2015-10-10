package edu.tce.cse.model;

public class EdgeData {
	public int count;
	public float weight;
	public EdgeData(){
		count = 0;
		weight = 0;
	}
	public EdgeData(int count, float weight){
		this.count = count;
		this.weight = weight;
	}
	public float getEdgeWeight(){
		return weight;
		//return (float)(weight/(count*1.0));
	}
}
