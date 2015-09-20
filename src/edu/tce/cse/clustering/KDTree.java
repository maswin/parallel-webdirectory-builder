package edu.tce.cse.clustering;

import edu.tce.cse.document.DocNode;

public class KDTree {
	
	private class KDNode{
		KDNode right;
		KDNode left;
		DocNode data;
		KDNode(DocNode data){
			this.data = data;
			this.left = null;
			this.right = null;
		}
	}
	
	KDNode root;
	int dimensions;
	
	KDTree(int dimensions){
		this.root = null;
		this.dimensions = dimensions;
	}
	
	public void insert(DocNode data){
		insertUtil(data, root, 0);
	}
	
	private void insertUtil(DocNode data, KDNode currentNode,
			int currentDimension){
		if(currentNode==null){
			currentNode = new KDNode(data);
			return;
		}else{
			if(data.tfIdf[currentDimension] < currentNode.data.tfIdf[currentDimension]){
				currentDimension = (currentDimension+1)%dimensions;
				insertUtil(data, currentNode.left, currentDimension);
			}else{
				currentDimension = (currentDimension+1)%dimensions;
				insertUtil(data, currentNode.right, currentDimension);
			}
		}
	}
	
	public int findDistance(DocNode node1, DocNode node2){
		int distance = 0;
		KDNode lcs = this.root;
		int currentDimension = 0;
		while(lcs!=null){
			if(node1.tfIdf[currentDimension] < lcs.data.tfIdf[currentDimension]
					&& node2.tfIdf[currentDimension] < lcs.data.tfIdf[currentDimension]){
				lcs = lcs.left;
			}else if(node1.tfIdf[currentDimension] > lcs.data.tfIdf[currentDimension] 
					&& node2.tfIdf[currentDimension] > lcs.data.tfIdf[currentDimension]){
				lcs = lcs.right;
			}else{
				break;
			}
			currentDimension = (currentDimension+1)%dimensions;
		}
		
		//Distance of node1 from lcs
		distance += findHeight(lcs, node1, currentDimension);
		
		//Distance of node2 from lcs
		distance += findHeight(lcs, node2, currentDimension);
		
		return distance;
	}
	
	private int findHeight(KDNode subTreeRoot, DocNode node, int currentDimension){
		int height = 0;
		
		while(subTreeRoot!=null){
			if(subTreeRoot.data.equals(node)){
				break;
			}
			if(node.tfIdf[currentDimension] < subTreeRoot.data.tfIdf[currentDimension]){
				subTreeRoot = subTreeRoot.left;
			}else {
				subTreeRoot = subTreeRoot.right;
			}
			height++;
			currentDimension = (currentDimension+1)%dimensions;
		}
		return height;
	}
}
