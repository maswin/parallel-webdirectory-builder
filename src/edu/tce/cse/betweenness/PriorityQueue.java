package edu.tce.cse.betweenness;

public interface PriorityQueue<E> {
	public void add(E item);
	public void decreasePriority(E item, double priority);
	public E extractMin();
	public void clear();
	public int size();
	public E findMin();
}