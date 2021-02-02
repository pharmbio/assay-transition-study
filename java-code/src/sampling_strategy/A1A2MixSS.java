package sampling_strategy;

import com.arosbio.modeling.data.Dataset;
import com.arosbio.modeling.data.Problem;
import com.arosbio.modeling.ml.ds_splitting.FoldedSampling;
import com.arosbio.modeling.ml.ds_splitting.TrainingsetSplitIterator;

public class A1A2MixSS extends FoldedSampling {

	public A1A2MixSS(int numFolds) {
		super(numFolds);
	}
	
	@Override
	public TrainingsetSplitIterator getIterator(Problem dataset) throws IllegalArgumentException {
		return super.getIterator(mix(dataset.getDataset(), dataset.getModelingExclusiveDataset()));
	}

	@Override
	public TrainingsetSplitIterator getIterator(Problem dataset, long seed) throws IllegalArgumentException {
		return super.getIterator(mix(dataset.getDataset(), dataset.getModelingExclusiveDataset()), seed);
	}
	
	private Problem mix(Dataset d1, Dataset d2) {
		Problem mix = new Problem();
		Dataset d = new Dataset();
		d.getRecords().addAll(d1.getRecords());
		d.getRecords().addAll(d2.getRecords());
		
		// altered!
		mix.setDataset(d);
		return mix;
	}
	
	public String toString() {
		return "A1A2mix";
	}
	
	public A1A2MixSS clone() {
		return new A1A2MixSS(getNumSamples());
	}

}
