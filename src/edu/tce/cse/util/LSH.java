package edu.tce.cse.util;

import java.util.Arrays;
import java.util.Random;

public class LSH {

	//private static final int D = 20; //Range
	private static final int modulo = 37; //Prime number within range (D,2D].
	protected int numOfBuckets = 10;
	protected int numOfFunctions = 10;
	protected int dimensions;
	protected int K = 5; //Code Depth - Increasing K increases LSH Strictness (Max Size 32)
	private int A[][] = null;
	/**
	 * Instantiates a LSH instance with s stages (or bands) and b buckets (per 
	 * stage), in a space with n dimensions.
	 * 
	 * @param b buckets (per stage)
	 * @param n dimensionality
	 */
	public LSH(int dimensions) {
		this.dimensions = dimensions;
	}
	public LSH(int dimension, int numOfBuckets){
		this.dimensions = dimensions;
		this.numOfBuckets = numOfBuckets;
	}
	public LSH(int dimensions, int numOfFunctions, int K){
		this.dimensions = dimensions;
		this.numOfFunctions = numOfFunctions;
		this.K = K;
	}
	public int getNumOfBuckets() {
		return numOfBuckets;
	}

	public void setNumOfBuckets(int numOfBuckets) {
		this.numOfBuckets = numOfBuckets;
	}

	public int getNumOfFunctions() {
		return numOfFunctions;
	}

	public void setNumOfFunctions(int numOfFunctions) {
		this.numOfFunctions = numOfFunctions;
	}

	public int getK() {
		return K;
	}

	public void setK(int k) {
		K = k;
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
		if(A==null){
			generateHashFunctions();
		}
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
	/*public int hash(boolean[] signature) {
		int hashSign = 0;       
		for(int i=0;i<signature.length;i++){
			if(signature[i]){
				hashSign++;
			} 
		}
		hashSign = hashSign%modulo;
		return hashSign%numOfBuckets;
	}*/


}
