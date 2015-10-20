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
import edu.tce.cse.model.Centroid;
import edu.tce.cse.model.Data;
import mpi.MPI;
import mpi.Op;

public class DistributedLSH {

	private List<Data> pairPoints;
	private int dimensions;
	private Set<String> flag;
	private LSH lsh;
	
	public DistributedLSH(int dimensions){
		pairPoints = new ArrayList<Data>();
		flag = new HashSet<String>();
		this.dimensions = dimensions;
	}

	public void hash(List<Centroid> nodeList){
		hash(nodeList.toArray(new Centroid[nodeList.size()]));		
	}

	public void printLSHParams(){
		System.out.println("Num of hash functions : "+lsh.l);
		System.out.println("Code depth : "+lsh.K);
	}
	public void hash(Centroid[] nodeList){
		lsh = new LSH(dimensions, 12, 4);
		if(MPI.COMM_WORLD.Rank()==0){
			printLSHParams();
		}
		if(nodeList.length<=0){
			System.out.println("List is Empty");
			return;
		}
		this.pairPoints = new ArrayList<Data>();

		Map<String, Set<Centroid>> localBuckets[] = new HashMap[1];
		Map<String, Set<Centroid>> globalBuckets[] = new HashMap[1];// = new HashMap<String, Set<DocNode>>();
		Set bucket;
		for(int i=0;i<lsh.l;i++){
			localBuckets[0] = new HashMap<String, Set<Centroid>>();
			for(Centroid node : nodeList){
				String hash = lsh.hash(i, node.tfIdf);
				if(localBuckets[0].containsKey(hash)){
					localBuckets[0].get(hash).add(node);
				}else{
					bucket = new HashSet<DocNode>();
					bucket.add(node);
					localBuckets[0].put(hash, bucket);
				}
			}
			globalBuckets[0] = new HashMap<String, Set<Centroid>>();
			MPI.COMM_WORLD.Reduce(localBuckets, 0, globalBuckets, 
					0, 1, MPI.OBJECT, new Op(new BucketReducer(), true),0);

			if(MPI.COMM_WORLD.Rank() == 0){
				processBucket(globalBuckets[0]);
			}
		}
	}
	public void processBucket(Map<String, Set<Centroid>> bucket){

		Centroid A,B;
		String flagCode;
		for(Map.Entry<String, Set<Centroid>> entry : bucket.entrySet()){
			List<Centroid> nodes = new ArrayList<>(entry.getValue());
			Collections.sort(nodes, (a,b) -> {
				if(a.clusterId < b.clusterId){
					return +1;
				}else{
					return -1;
				}
			});
			for(int i=0;i<nodes.size();i++){
				for(int j=i+1;j<nodes.size();j++){
					A = nodes.get(i);
					B = nodes.get(j);
					flagCode = ""+A.clusterId+B.clusterId;
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
		System.out.println("Reduced No. of Comparisions :"+pairPoints.size());
		return pairPoints;
	}
}