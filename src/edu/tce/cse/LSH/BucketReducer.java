package edu.tce.cse.LSH;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.tce.cse.document.DocNode;
import edu.tce.cse.model.Centroid;
import mpi.Datatype;
import mpi.MPIException;
import mpi.User_function;

public class BucketReducer extends User_function{

	@Override
	public void Call(Object localBucket, int localOffset, Object globalBucket, 
			int globalOffset, int count, Datatype dataType) throws MPIException {
		// TODO Auto-generated method stub
		Map<String, Set<Centroid>> inBucket = 
                ((Map<String, Set<Centroid>>[]) localBucket)[0];
		
		
		Object[] inOut = (Object[]) globalBucket;
		
		Map<String, Set<Centroid>> inOutBucket = 
                (Map<String, Set<Centroid>>) inOut[0];
		 
		for(Map.Entry<String, Set<Centroid>> entry : inBucket.entrySet()){
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
