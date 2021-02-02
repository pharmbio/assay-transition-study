package sampling_strategy;

import com.arosbio.modeling.ml.ds_splitting.FoldedSampling;

/**
 * The Assay Transition strategy is actually the default way given how we read in the datasets.
 * No need to do any alterations here
 * @author staffan
 *
 */
public class ATSS extends FoldedSampling {

	public ATSS(int numFolds) {
		super(numFolds);
	}
	
	public String toString() {
		return "AT";
	}

}
