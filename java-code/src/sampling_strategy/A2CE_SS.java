package sampling_strategy;

import com.arosbio.modeling.data.Dataset;
import com.arosbio.modeling.data.Problem;
import com.arosbio.modeling.ml.ds_splitting.FoldedSampling;
import com.arosbio.modeling.ml.ds_splitting.TrainingsetSplitIterator;

/**
 * A2 Calibration Exclusive (i.e. appended to the calibration set every time)
 * @author staffan
 *
 */
public class A2CE_SS extends FoldedSampling {

	public A2CE_SS(int numFolds) {
		super(numFolds);
	}
	
	@Override
	public TrainingsetSplitIterator getIterator(Problem dataset) throws IllegalArgumentException {
		return super.getIterator(getProblem(dataset.getDataset(), dataset.getModelingExclusiveDataset()));
	}

	@Override
	public TrainingsetSplitIterator getIterator(Problem dataset, long seed) throws IllegalArgumentException {
		return super.getIterator(getProblem(dataset.getDataset(), dataset.getModelingExclusiveDataset()), seed);
	}
	
	private Problem getProblem(Dataset a2, Dataset a1) {
		Problem mix = new Problem();
		mix.setCalibrationExclusiveDataset(a2);
		mix.setDataset(a1);
		return mix;
	}
	
	public String toString() {
		return "A2_CE";
	}

	public A2CE_SS clone() {
		return new A2CE_SS(getNumSamples());
	}
}
