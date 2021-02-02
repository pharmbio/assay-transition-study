package evaluateDatasets;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.arosbio.commons.CollectionUtils;
import com.arosbio.modeling.CPSignSettings;
import com.arosbio.modeling.data.Problem;
import com.arosbio.modeling.ml.algorithms.svm.LinearSVC;
import com.arosbio.modeling.ml.cp.acp.ACPClassification;
import com.arosbio.modeling.ml.cp.icp.ICPClassificationNCM;
import com.arosbio.modeling.ml.cp.nonconf.calc.LinearInterpolationPValue;
import com.arosbio.modeling.ml.cp.nonconf.classification.NegativeDistanceToHyperplaneNCM;
import com.arosbio.modeling.ml.ds_splitting.SamplingStrategy;
import com.arosbio.modeling.ml.metrics.Metric;
import com.arosbio.modeling.ml.metrics.SingleValuedMetric;
import com.arosbio.modeling.ml.metrics.plots.MergedPlot;
import com.arosbio.modeling.ml.metrics.plots.Plot2D;
import com.arosbio.modeling.ml.metrics.plots.PlotMetric;
import com.arosbio.modeling.ml.testing.TestRunner;

import cv_folds.ATTestStrategy;
import utils.Utils;

class ClassificationRunner {

	public int K_folds = 10;

	private static final double cost = 0.25;

	private static final List<Double> CONFIDENCES_CV = CollectionUtils.listRange(0d, 1d, 0.05);

	public ClassificationRunner() {
	}

	public void run(Problem problem, String logFile, SamplingStrategy samplStrat, int a2_size, BufferedWriter resultsWriter, boolean addHeader) throws Exception {
		
		int a1_size = problem.getModelingExclusiveDataset().size();
		
		// Config params
		LinearSVC svc = new LinearSVC();
		svc.setC(cost);
		ICPClassificationNCM icp = new ICPClassificationNCM(new NegativeDistanceToHyperplaneNCM(svc));
		icp.setPValueCalculator(new LinearInterpolationPValue());
		ACPClassification acp = new ACPClassification(
				icp, 
				samplStrat);

		// CV params
		TestRunner tester = new TestRunner(new ATTestStrategy(K_folds, CPSignSettings.getInstance().getRNGSeed(), a2_size),CONFIDENCES_CV);

		long startTime = System.currentTimeMillis();
		List<Metric> mets = tester.evaluate(problem, acp);
		long stopTime = System.currentTimeMillis();
		
		Map<String, Object> nonPlotData = new LinkedHashMap<>();
		
		List<PlotMetric> plotMetrics = new ArrayList<>();
		for (Metric m : mets) {
			if (m instanceof SingleValuedMetric) {
				nonPlotData.put(m.getName(), ((SingleValuedMetric) m).getScore());
			} else if (m instanceof PlotMetric) {
				plotMetrics.add((PlotMetric) m);
			} else {
				throw new RuntimeException("FAILED DUE TO METRIC OF UN-REC TYPE: " + m);
			}
		}
		
		// Add extra things to the single-valued metric
		nonPlotData.put("A1_size", a1_size);
		nonPlotData.put("A2_size", a2_size);
		nonPlotData.put("SamplingStrategy", samplStrat.toString());
		nonPlotData.put("Runtime(sec)", (stopTime-startTime)/1000);
		nonPlotData.put("Seed", CPSignSettings.getInstance().getRNGSeed());

		List<Plot2D> plots = new ArrayList<>();
		for (PlotMetric pm: plotMetrics) {
			plots.add(pm.buildPlot());
		}
		if (addHeader)
			resultsWriter.write(new MergedPlot(plots).toCSV(Utils.RESULTS_CSV_DELIM, nonPlotData));
		else {
			resultsWriter.write(new MergedPlot(plots).toCSV(Utils.RESULTS_CSV_DELIM, nonPlotData).split("\n", 2)[1]); // Remove the header line
		}
		resultsWriter.flush();
		
		// this is printed to the sbatch output just in case
		System.out.println("Final runtime: " + (stopTime-startTime)/1000);
		System.out.println("As string: " + mets);
	}

}
