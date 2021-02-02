package sampling_strategy;

import com.arosbio.modeling.data.Dataset;
import com.arosbio.modeling.data.Problem;
import com.arosbio.modeling.ml.ds_splitting.FoldedSampling;
import com.arosbio.modeling.ml.ds_splitting.TrainingsetSplitIterator;

public class A1SS extends FoldedSampling {

	public A1SS(int numFolds) {
		super(numFolds);
	}
	
	@Override
	public TrainingsetSplitIterator getIterator(Problem dataset) throws IllegalArgumentException {
		return super.getIterator(getProblem(dataset.getModelingExclusiveDataset()));
	}

	@Override
	public TrainingsetSplitIterator getIterator(Problem dataset, long seed) throws IllegalArgumentException {
		return super.getIterator(getProblem(dataset.getModelingExclusiveDataset()), seed);
	}
	
	private Problem getProblem(Dataset d1) {
		Problem mix = new Problem();
		mix.setDataset(d1.clone());
		return mix;
	}

	public String toString() {
		return "A1";
	}
	
	public A1SS clone() {
		return new A1SS(getNumSamples());
	}
}
