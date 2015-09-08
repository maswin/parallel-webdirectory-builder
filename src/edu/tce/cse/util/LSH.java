package edu.tce.cse.util;

import java.util.Arrays;
import java.util.Random;

public class LSH {
    
	//private static final int D = 20; //Range
	//private static final int modulo = 37; //Prime number within range (D,2D].
    protected int numOfBuckets;
    protected int numOfFunctions;
    protected int dimensions;
    protected int K; //Code Depth - Increasing K increases LSH Strictness (Max Size 32)
    private int A[][];
    /**
     * Instantiates a LSH instance with s stages (or bands) and b buckets (per 
     * stage), in a space with n dimensions.
     * 
     * @param b buckets (per stage)
     * @param n dimensionality
     */
    public LSH(int numOfBuckets, int numOfFunctions, int dimensions, int K) {
        this.numOfBuckets = numOfBuckets; 
        this.numOfFunctions = numOfFunctions;
        this.dimensions = dimensions;
        this.K = K;
        generateHashFunctions();
    }
    
    private void generateHashFunctions(){
    	A = new int[numOfFunctions][K];
    	Random r = new Random();
    	for(int i=0;i<numOfFunctions;i++){
    		for(int j=0;j<K;j++){
    			A[i][j] = r.nextInt(dimensions);
    		}
    	}
    	/*for(int i=0;i<numOfFunctions;i++){
    		System.out.println(Arrays.toString(A[i]));
    	}*/
    }
    /**
     * Hash a signature.
     * value = (AX+B)%numOfBuckets
     * @param signature
     * @return An vector of b integers (between 0 and b-1)
     */
    public int[] hashSignature(boolean[] signature) {
         int[] hashSign = new int[numOfFunctions];       
         for(int i=0;i<numOfFunctions;i++){
        	 hashSign[i] = hash(i,signature);
         }
         return hashSign;
    }
    public int hash(int index,boolean[] signature){
    	String bucket = "";
    	int bucketValue = 0;
    	for(int i=0;i<K;i++){
    		if(signature[A[index][i]]){
    			bucket += '1';
    		}else{
    			bucket += '0';
    		}
    	}
    	
    	bucketValue = Integer.parseInt(bucket,2);
    	return bucketValue;
    }
    
}
