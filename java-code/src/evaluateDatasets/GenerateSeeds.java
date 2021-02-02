package evaluateDatasets;

import java.util.Random;

public class GenerateSeeds {
	
	private static final int NUM_SEEDS = 40;

	public static void main(String[] args) {
		Random rand = new Random();
	    //Store a random seed
		for (int i=0; i< NUM_SEEDS; i++) {
			long seed = rand.nextLong();
			System.out.print(seed+" ");
//			rand.setSeed(seed);
		}
	}

}
