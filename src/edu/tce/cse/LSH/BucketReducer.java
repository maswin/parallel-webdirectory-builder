package edu.tce.cse.LSH;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.tce.cse.document.DocNode;
import mpi.Datatype;
import mpi.MPIException;
import mpi.User_function;

public class BucketReducer extends User_function{

	@Override
	public void Call(Object localBucket, int localOffset, Object globalBucket, 
			int globalOffset, int count, Datatype dataType) throws MPIException {
		// TODO Auto-generated method stub
		Map<String, Set<DocNode>> inBucket = 
                ((Map<String, Set<DocNode>>[]) localBucket)[0];
		
		
		Object[] inOut = (Object[]) globalBucket;
		
		Map<String, Set<DocNode>> inOutBucket = 
                (Map<String, Set<DocNode>>) inOut[0];
		 
		for(Map.Entry<String, Set<DocNode>> entry : inBucket.entrySet()){
			String hash = entry.getKey();
			Set bucket = entry.getValue();
			if(inOutBucket.containsKey(hash)){
				inOutBucket.get(hash).addAll(bucket);
			}else{
				inOutBucket.put(hash, bucket);
			}
		}
	}

}
