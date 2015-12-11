package edu.tce.cse.clustering;

import java.io.Serializable;

public class Edge implements Serializable{
	private long src;
	private long dst;
	private float weight;
	public Edge(long src, long dst, float weight){
		this.src = src;
		this.dst = dst;
		this.weight = weight;
	}
	public long getSrc() {
		return src;
	}
	public void setSrc(long src) {
		this.src = src;
	}
	public long getDst() {
		return dst;
	}
	public void setDst(long dst) {
		this.dst = dst;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
}