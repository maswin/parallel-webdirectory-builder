package edu.tce.cse.LSH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.tce.cse.document.DocNode;
import edu.tce.cse.model.Data;
import mpi.MPI;
import mpi.Op;

public class DistributedLSH {

	private List<Data> pairPoints;
	private Set<String> flag;
	public DistributedLSH(){
		pairPoints = new ArrayList<Data>();
		flag = new HashSet<String>();
	}
	public void hash(List<DocNode> nodeList){
		hash(nodeList.toArray(new DocNode[nodeList.size()]));
		
	}
	public void hash(DocNode[] nodeList){
		if(nodeList.length<=0){
			System.out.println("List is Empty");
			return;
		}
		LSH lsh = new LSH(nodeList[0].getSignature().length, 30, 4, 70);
		
		Map<String, Set<DocNode>> localBuckets[] = new HashMap[1];
		Map<String, Set<DocNode>> globalBuckets[] = new HashMap[1];// = new HashMap<String, Set<DocNode>>();
		Set bucket;
		for(int i=0;i<lsh.getNumOfFunctions();i++){
			localBuckets[0] = new HashMap<String, Set<DocNode>>();
			for(DocNode node : nodeList){
				String hash = lsh.hash(i, node.signature);
				if(localBuckets[0].containsKey(hash)){
					localBuckets[0].get(hash).add(node);
				}else{
					bucket = new HashSet<DocNode>();
					bucket.add(node);
					localBuckets[0].put(hash, bucket);
				}
			}
			globalBuckets[0] = new HashMap<String, Set<DocNode>>();
			MPI.COMM_WORLD.Reduce(localBuckets, 0, globalBuckets, 
					0, 1, MPI.OBJECT, new Op(new BucketReducer(), true),0);

			if(MPI.COMM_WORLD.Rank() == 0){
				processBucket(globalBuckets[0]);
			}
		}
		
	}
	public void processBucket(Map<String, Set<DocNode>> bucket){
		
		DocNode A,B;
		String flagCode;
		for(Map.Entry<String, Set<DocNode>> entry : bucket.entrySet()){
			List<DocNode> nodes = new ArrayList<>(entry.getValue());
			Collections.sort(nodes, (a,b) -> {
				if(a.nodeID < b.nodeID){
					return +1;
				}else{
					return -1;
				}
			});
			for(int i=0;i<nodes.size();i++){
				for(int j=i+1;j<nodes.size();j++){
					A = nodes.get(i);
					B = nodes.get(j);
					flagCode = ""+A.nodeID+B.nodeID;
					if(!flag.contains(flagCode)){
						flag.add(flagCode);
						Data data = new Data(A,B);
						pairPoints.add(data);
					}
				}
			}
		}
	}
	
	public List<Data> getPairPoints(){
		return pairPoints;
	}
}
