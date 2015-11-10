package edu.tce.cse.LSH;

import java.util.Arrays;
import java.util.Random;

import edu.tce.cse.util.SuperBit;
import mpi.MPI;

public class LSH {

	protected int dimensions;
	protected int K = 5; //Signature Length - Increasing K increases LSH Strictness (Max Size 32)
	protected int l = 10; //Num of LSH Functions - Decreasing l increases LSH Strictness
	SuperBit[] sb;
	
	public LSH(int dimensions){
		this.dimensions = dimensions;
		generateSuperBit();
	}
	
	public LSH(int dimensions, int l, int K){
		this.dimensions = dimensions;
		this.l = l;
		this.K = K;
		generateSuperBit();
	}

	public void generateSuperBit(){
		this.sb = new SuperBit[l];
		if(MPI.COMM_WORLD.Rank()==0){
			System.out.println("New Super Bit Generated");
			for(int i=0; i<l; i++){
				sb[i] = new SuperBit(this.dimensions, this.K, 1);
			}
		}		
		MPI.COMM_WORLD.Bcast(sb, 0, l, MPI.OBJECT, 0);
	}
	
	public String[] hashSignature(double[] tfIdf) {
		String[] hashSign = new String[l];       
		for(int i=0;i<l;i++){
			hashSign[i] = hash(i, tfIdf);
		}
		return hashSign;
	}
	public String hash(int index,double[] tfIdf){
		StringBuilder bucket = new StringBuilder();
		boolean[] signature = this.sb[index].signature(tfIdf);
		for(int i=0;i<signature.length;i++){
			if(signature[i]){
				bucket.append("1");
			}else{
				bucket.append("0");
			}
		}
		return bucket.toString();
	}

}