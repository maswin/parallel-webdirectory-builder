package edu.tce.cse.betweenness;

import edu.tce.cse.clustering.DocNode;
import edu.tce.cse.betweenness.FibonacciHeap.Node;

public class FibonacciPriorityQueue implements PriorityQueue<DocNode> {
	
	FibonacciHeap<DocNode> heap = new FibonacciHeap<DocNode>();
	int heapSize = 0;
	
	public void add(DocNode item) {
		Node<DocNode> node = heap.insert(item);
		item.node = node;
		++heapSize;
	}

	public void decreasePriority(DocNode item, double priority) {
		item.priority = (float)priority;
		heap.decreaseKey(item.node, item);
	}

	public DocNode extractMin() {
		if (heapSize > 0) {
			--heapSize;
			return heap.extractMin().getKey();
		} else {
			return null;
		}
	}
	
	public DocNode findMin(){
		return heap.findMinimum().getKey();
	}

	public void clear() {
		heap.clear();
		heapSize = 0;
	}

	public int size() {
		return heapSize;
	}	

}
