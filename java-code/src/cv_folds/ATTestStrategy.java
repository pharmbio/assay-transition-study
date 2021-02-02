package cv_folds;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.arosbio.modeling.data.Dataset;
import com.arosbio.modeling.data.Problem;
import com.arosbio.modeling.ml.testing.KFoldCVSplitter;
import com.arosbio.modeling.ml.testing.TestTrainSplit;
import com.arosbio.modeling.ml.testing.TestingStrategy;

import utils.Utils;

public class ATTestStrategy implements TestingStrategy {
	
	private final int numFolds;
	private long seed;
	private int a2_size;
	
	public ATTestStrategy(int numFolds, long seed, int a2_size) {
		this.numFolds = numFolds;
		this.seed = seed;
		this.a2_size = a2_size;
	}

	@Override
	public int getNumberOfSplitsAndValidate(Problem data) throws IllegalArgumentException {
		return numFolds;
	}

	@Override
	public long getRNGSeed() {
		return seed;
	}
	
	@Override
	public Iterator<TestTrainSplit> getSplits(Problem data) {
		return new A2_KFoldSplitter(data);
	}

	@Override
	public void setRNGSeed(long seed) {
		this.seed = seed;
	}

	
	private class A2_KFoldSplitter implements Iterator<TestTrainSplit> {

		private Iterator<TestTrainSplit> a2_splitter;
		private Dataset A1;
		
		public A2_KFoldSplitter(Problem p) {
			if (! p.getModelingExclusiveDataset().isEmpty()) {
				A1 = p.getModelingExclusiveDataset();
				A1.shuffle(seed);
			}
			
			Problem a2prob = new Problem();
			a2prob.setDataset(p.getDataset());
			
			a2_splitter = new KFoldCVSplitter(numFolds, seed).getSplits(a2prob);
		}
		
		@Override
		public boolean hasNext() {
			return a2_splitter.hasNext();
		}

		@Override
		public TestTrainSplit next() {
			if (! hasNext()) {
				throw new NoSuchElementException();
			}
			
			TestTrainSplit a2_data = a2_splitter.next();
			
			// The testing-strategy also has to sub-sample the A2 dataset to specified size
			Dataset a2 = a2_data.getTrainingSet().getDataset();
			a2 = Utils.subsample(a2, seed, a2_size);
			
			// Assemble the final training data
			Problem trainData = new Problem();
			trainData.setDataset(a2);
			if (A1 != null)
				trainData.setModelingExclusiveDataset(A1.clone());

			return new TestTrainSplit(trainData, a2_data.getTestSet());
		}
		
	}


	@Override
	public List<ConfigParameter> getConfigParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConfigParameters(Map<String, Object> params) throws IllegalStateException, IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}
	

}
