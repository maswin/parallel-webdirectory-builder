package edu.tce.cse.clustering;

public class Edge<E extends Node>{
	private E src;
	private E dst;
	private float weight;
	public Edge(E src, E dst, float weight){
		this.src = src;
		this.dst = dst;
		this.weight = weight;
	}
	public E getSrc() {
		return src;
	}
	public void setSrc(E src) {
		this.src = src;
	}
	public E getDst() {
		return dst;
	}
	public void setDst(E dst) {
		this.dst = dst;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
}