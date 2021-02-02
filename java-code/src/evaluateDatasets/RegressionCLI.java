package evaluateDatasets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.arosbio.modeling.CPSignSettings;
import com.arosbio.modeling.data.Dataset;
import com.arosbio.modeling.data.Problem;
import com.arosbio.modeling.ml.ds_splitting.SamplingStrategy;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import sampling_strategy.A1A2MixSS;
import sampling_strategy.A1SS;
import sampling_strategy.A2CE_SS;
import sampling_strategy.A2SS;
import sampling_strategy.ATSS;
import sampling_strategy.ICPSamplingStrategy;
import utils.Utils;

public class RegressionCLI {

	public static final int NUM_CV_FOLDS = 10;

	public static class CLIParams {

		@Parameter(names = {"--strategy", "-st"}, required = true, 
				description = "Strategy "
						+ "(1) A2, "
						+ "(2) assay-transition, "
						+ "(3) A1+A2, "
						+ "(4) A1=normal, A2=calib exclusive, "
						+ "(5) A1, "
						+ "(6) ICP A1=prop, A2=calib")
		int strategy;

		@Parameter(names= {"--new-assay", "-na"}, required = true)
		String newAssayData;

		@Parameter(names= {"--old-assays", "-oa"}, required = false)
		String oldAssayData;

		@Parameter(names= {"--seed"}, required = true)
		long seed;

		@Parameter(names = "--cost")
		Double cost;

		@Parameter(names = "--eps-svr")
		Double epsSVR;

		@Parameter(names = {"--help"}, help=true)
		boolean help = false;

		@Parameter(names = {"--a1-size"}, description = "Fixed A1 (A old) size")
		int a1Size = -1;

		@Parameter(names = {"--a2-size"}, description = "Fixed A2 (A new) size(s)")
		List<Integer> a2Size;

		@Parameter(names = {"--ccp"}, description = "Number of CCP folds")
		int ccpFolds = 10;

		@Parameter(names = {"--echo"}, description = "Echo inpit arguments")
		boolean echo = true;

		@Parameter(names = {"-o", "--output"}, required=true, description="path to where the results should be printed")
		File resultsFile;
	}

	private static void printArgsAndExit() {
		JCommander.newBuilder().addObject(new CLIParams()).build().usage();
		System.exit(0);
	}

	public static void main(String[] args) {

		try {
			run(args);
		} catch(Exception e) {
			System.err.println("Failed!!");
			e.printStackTrace();
		}

	}

	public static void run(String [] args) throws Exception {
		if (args.length == 0)
			printArgsAndExit();

		CLIParams params = new CLIParams();

		JCommander jc = JCommander.newBuilder().addObject(params).build();

		try {
			jc.parse(args);
		} catch(ParameterException e) {
			System.err.println(e.getMessage());
			System.out.println("\n");
			printArgsAndExit();
		}

		if (params.help) {
			printArgsAndExit();
		}

		System.out.println("Running params: ");
		for (String arg: args)
			System.out.print(arg + ' ');
		System.out.println();

		Utils.verifyCPSign();

		// Set the seed and the magic will happen
		CPSignSettings.getInstance().setRNGSeed(params.seed);

		if (params.a2Size == null || params.a2Size.isEmpty())
			params.a2Size = Arrays.asList(-1);

		// Load dataset(s)
		Dataset ds = null;
		try (InputStream stream = new FileInputStream(new File(params.newAssayData))){
			ds = Dataset.fromLIBSVMFormat(stream);
		}
		Problem prob = new Problem();
		prob.setDataset(ds);

		if (params.strategy!=1) {

			if (params.oldAssayData == null) {
				throw new IllegalArgumentException("A1 dataset must be given for all strategies != 1");
			}
			Dataset old = null;
			try (InputStream stream = new FileInputStream(new File(params.oldAssayData))){
				old = Dataset.fromLIBSVMFormat(stream);
			}
			old.shuffle(params.seed);
			if (params.a1Size >= old.size() || params.a1Size < 0) {
				prob.setModelingExclusiveDataset(old);
			} else {
				prob.setModelingExclusiveDataset(old.splitStatic(params.a1Size)[0]);
			}
			System.out.println("Running with |A1|="+prob.getModelingExclusiveDataset().size());

		}

		if (params.strategy == 5) {
			System.out.println("Running A1 only strategy, setting a single A2 size");
			params.a2Size = Arrays.asList(-1);
		}

		boolean addHeader = true;
		try (BufferedWriter resWriter = new BufferedWriter(new FileWriter(params.resultsFile))){


			// Run for each A2-size
			RegressionRunner runner = new RegressionRunner();
			SamplingStrategy ss = null;
			for (int a2 : params.a2Size) {

				// Set up fold generator depending on strategy
				switch (params.strategy) {
				case 1:
					// A2
					System.out.println("Running A2 with seed: " + params.seed + ", ccpFolds=" + params.ccpFolds + ", A2-size: " + a2);
					ss = new A2SS(params.ccpFolds);
					break;
				case 2:
					// Assay transition
					System.out.println("Running ASSAY-TRANSITION with seed: " + params.seed +  ", ccpFolds=" + params.ccpFolds+ ", A2-size: " + a2);
					ss = new ATSS(params.ccpFolds);
					break;
				case 3: 
					// A1 + A2 mix
					System.out.println("Running A1+A2 with seed: " + params.seed + ", ccpFolds=" + params.ccpFolds+ ", A2-size: " + a2);
					ss = new A1A2MixSS(params.ccpFolds);
					break;
				case 4:
					// A1=normal, A2 = calibration exclusive
					System.out.println("Running A1=normal, A2=CALIBRATION EXCLUSIVE with seed: " + params.seed + ", ccpFolds=" + params.ccpFolds+ ", A2-size: " + a2);
					ss = new A2CE_SS(params.ccpFolds);
					break;
				case 5:
					// A1 
					System.out.println("Running A1 with seed: " + params.seed + ", ccpFolds=" + params.ccpFolds+ ", A2-size: " + a2);
					ss = new A1SS(params.ccpFolds);
					break;
				case 6:
					System.out.println("Running ICP with seed: " + params.seed+ ", A2-size: " + a2);
					ss = new ICPSamplingStrategy();
					break;
				default:
					System.err.println("Faulty strategy: " + params.strategy);
					printArgsAndExit();
				}


				try {
					runner.run(prob, ss, a2, resWriter, addHeader);
				} catch (Exception e) {
					System.out.println("Failed execution for |A2|="+a2);
					e.printStackTrace();
					continue;
				}
				addHeader = false;
			}
		}
	}
}
