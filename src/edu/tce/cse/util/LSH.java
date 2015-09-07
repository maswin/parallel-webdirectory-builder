package edu.tce.cse.util;

import java.util.Random;

public class LSH {
    
	private static final int D = 20; //Range
	private static final int modulo = 37; //Prime number within range (D,2D].
    protected int numOfBuckets;
    protected int numOfFunctions;
    protected int dimensions;
    
    private int A[][];
    private int B[];
    /**
     * Instantiates a LSH instance with s stages (or bands) and b buckets (per 
     * stage), in a space with n dimensions.
     * 
     * @param b buckets (per stage)
     * @param n dimensionality
     */
    public LSH(int numOfBuckets, int numOfFunctions, int dimensions) {
        this.numOfBuckets = numOfBuckets; 
        this.numOfFunctions = numOfFunctions;
        this.dimensions = dimensions;
        generateHashFunctions();
    }
    
    private void generateHashFunctions(){
    	A = new int[numOfFunctions][dimensions];
    	B = new int[numOfFunctions];
    	Random r = new Random();
    	for(int i=0;i<numOfFunctions;i++){
    		for(int j=0;j<dimensions;j++){
    			A[i][j] = r.nextInt(D);
    		}
    		B[i] = r.nextInt(D);
    	}
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
    	int bucket = 0;
    	for(int i=0;i<dimensions;i++){
    		if(signature[i]){
    			bucket += A[index][i];
    		}
    	}
    	bucket += B[index];
    	bucket = (bucket%modulo)%numOfBuckets;
    	return bucket;
    }
    
}
