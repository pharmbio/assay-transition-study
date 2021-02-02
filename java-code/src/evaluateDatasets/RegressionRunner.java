package evaluateDatasets;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.arosbio.commons.CollectionUtils;
import com.arosbio.modeling.CPSignSettings;
import com.arosbio.modeling.data.Problem;
import com.arosbio.modeling.ml.algorithms.svm.LinearSVR;
import com.arosbio.modeling.ml.cp.acp.ACPRegression;
import com.arosbio.modeling.ml.cp.icp.ICPRegressionNCM;
import com.arosbio.modeling.ml.cp.nonconf.calc.LinearInterpolationPValue;
import com.arosbio.modeling.ml.cp.nonconf.regression.LogNormalizedNCM;
import com.arosbio.modeling.ml.ds_splitting.SamplingStrategy;
import com.arosbio.modeling.ml.metrics.Metric;
import com.arosbio.modeling.ml.metrics.MetricBuilderFactory;
import com.arosbio.modeling.ml.metrics.SingleValuedMetric;
import com.arosbio.modeling.ml.metrics.plots.MergedPlot;
import com.arosbio.modeling.ml.metrics.plots.Plot2D;
import com.arosbio.modeling.ml.metrics.plots.PlotMetric;
import com.arosbio.modeling.ml.testing.TestRunner;

import cv_folds.ATTestStrategy;
import utils.Utils;

class RegressionRunner {

	public int K_folds = 10;

	private static final double cost = 0.25;
	private static final double svrEpsilon = 0.01;

	private static final List<Double> CONFIDENCES_CV = CollectionUtils.listRange(0d, 1d, 0.05);

	public RegressionRunner() {
	}

	public void run(Problem problem, SamplingStrategy ss, int a2_size, BufferedWriter resultsWriter, boolean addHeader) throws Exception {
		
		int a1_size = problem.getModelingExclusiveDataset().size();
		System.out.println("Running with ds.size=" + problem.getDataset().size() + ", prop-train.size="
				+ problem.getModelingExclusiveDataset().size());

		// Config params
		LinearSVR svr = new LinearSVR();
		svr.setC(cost);
		svr.setSVREpsilon(svrEpsilon);
		ICPRegressionNCM icp = new ICPRegressionNCM(new LogNormalizedNCM(svr, .01));
		icp.setPValueCalculator(new LinearInterpolationPValue());
		ACPRegression acp = new ACPRegression(
				icp,
				ss);

		// CV params
		TestRunner runner = new TestRunner(new ATTestStrategy(K_folds, CPSignSettings.getInstance().getRNGSeed(), a2_size), CONFIDENCES_CV);
		
		long startTime = System.currentTimeMillis();
		// Use all default ones *and* the distance-based ones!
		List<Metric> metsInput = MetricBuilderFactory.getACPRegressionMetrics();
		
		List<Metric> mets = runner.evaluate(problem, acp, metsInput);
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
		nonPlotData.put("SamplingStrategy", ss.toString());
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
