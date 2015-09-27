package edu.tce.cse.LSH;

import java.util.Arrays;
import java.util.Random;

import mpi.MPI;

public class LSH {

	protected int numOfFunctions = 10;
	protected int dimensions;
	protected int K = 5; //Code Depth - Increasing K increases LSH Strictness (Max Size 32)
	protected int C;
	protected double r;
	private int A[][] = null;

	private double Pmiss = 0.5;

	public LSH(int dimensions){
		this.dimensions = dimensions;
		generateHashFunctions();
	}
	public int getNumOfFunctions() {
		return numOfFunctions;
	}
	public void setNumOfFunctions(int numOfFunctions) {
		this.numOfFunctions = numOfFunctions;
	}
	public int getDimensions() {
		return dimensions;
	}
	public void setDimensions(int dimensions) {
		this.dimensions = dimensions;
	}

	public LSH(int dimensions, int C, double r){
		this.dimensions = dimensions;
		this.C = C;
		this.r = r;
		setK();
		setNumOfHashFunctions();
		generateHashFunctions();
	}

	/*
	 * Set K as
	 * K = (dC/2r)*root(d) 
	 */
	private void setK(){		
		this.K = (int) (((dimensions*C)/(2*r))*Math.sqrt(dimensions));		
	}
	/*
	 * Set l(numOfHashFunctions) as
	 * Pmiss = (1-(1-(r/Cd))^k)^l
	 * val = 1-(r/Cd)
	 * l = log(Pmiss) / log (1-(val^k))
	 */
	private void setNumOfHashFunctions(){
		double val = 1 - (r/(C*dimensions));
		this.numOfFunctions = (int) (Math.log(Pmiss)/Math.log(1-(Math.pow(val, K))));
	}
	private void generateHashFunctions(){
		A = new int[numOfFunctions][K];
		if(MPI.COMM_WORLD.Rank()==0){
			System.out.println(K+" "+numOfFunctions);
			Random r = new Random();
			for(int i=0;i<numOfFunctions;i++){
				for(int j=0;j<K;j++){
					A[i][j] = r.nextInt(dimensions);
				}
			}
		}
		
		MPI.COMM_WORLD.Bcast(A, 0, numOfFunctions, MPI.OBJECT, 0);
		MPI.COMM_WORLD.Barrier();
		/*for(int i=0;i<numOfFunctions;i++){
    		System.out.println(Arrays.toString(A[i]));
    	}*/
	}
	public String[] hashSignature(boolean[] signature) {
		String[] hashSign = new String[numOfFunctions];       
		for(int i=0;i<numOfFunctions;i++){
			hashSign[i] = hash(i,signature);
		}
		return hashSign;
	}
	public String hash(int index,boolean[] signature){
		String bucket = "";
		for(int i=0;i<K;i++){
			if(signature[A[index][i]]){
				bucket += '1';
			}else{
				bucket += '0';
			}
		}
		return bucket;
	}

}
