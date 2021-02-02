package sampling_strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arosbio.modeling.data.Dataset;
import com.arosbio.modeling.data.Problem;
import com.arosbio.modeling.ml.ds_splitting.SamplingStrategy;
import com.arosbio.modeling.ml.ds_splitting.TrainingsetSplit;
import com.arosbio.modeling.ml.ds_splitting.TrainingsetSplitIterator;

public class ICPSamplingStrategy implements SamplingStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(ICPSamplingStrategy.class);

	public ICPSamplingStrategy() {}

	public String toString() {
		return "ICP";
	}

	@Override
	public ICPSamplingStrategy clone() {
		return new ICPSamplingStrategy();
	}

	@Override
	public TrainingsetSplitIterator getIterator(Problem dataset) throws IllegalArgumentException {
		LOGGER.debug("Running ICP with prop.size=" + dataset.getModelingExclusiveDataset().size() + ", calib.size="+dataset.getDataset().size());
		return new TrainingSplitsIterator(dataset.getModelingExclusiveDataset(), dataset.getDataset());
	}

	@Override
	public TrainingsetSplitIterator getIterator(Problem dataset, long seed) throws IllegalArgumentException {
		return new TrainingSplitsIterator(dataset.getModelingExclusiveDataset(), dataset.getDataset());
	}

	@Override
	public int getNumSamples() {
		return 1;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<String, Object>();
	}

	@Override
	public boolean isFolded() {
		return false;
	}

	@Override
	public boolean isStratified() {
		return false;
	}

	public class TrainingSplitsIterator implements TrainingsetSplitIterator {

		private final Dataset properTrain, calibSet;
		private boolean hasIterated = false;

		public TrainingSplitsIterator(Dataset properTrain, Dataset calibset) {
			this.properTrain = properTrain;
			this.calibSet = calibset;
		}

		@Override
		public boolean hasNext() {
			return !hasIterated;
		}

		@Override
		public TrainingsetSplit next() {
			if (hasIterated)
				throw new NoSuchElementException();
			hasIterated = true;
			return get(0);
		}

		@Override
		public TrainingsetSplit get(int index) throws NoSuchElementException {
			if (index != 0)
				throw new NoSuchElementException();

			return new TrainingsetSplit(properTrain.getRecords(), calibSet.getRecords());
		}

		@Override
		public int getMaximumSplitIndex() {
			return 0;
		}

		@Override
		public int getMinimumSplitIndex() {
			return 0;
		}

		@Override
		public Problem getProblem() {
			Problem p = new Problem();
			p.setCalibrationExclusiveDataset(calibSet);
			p.setModelingExclusiveDataset(properTrain);
			return p;
		}

	}

	@Override
	public List<ConfigParameter> getConfigParameters() {
		// do nothing
		return new ArrayList<>();
	}

	@Override
	public void setConfigParameters(Map<String, Object> params) throws IllegalStateException, IllegalArgumentException {
		// do nothing
	}

	@Override
	public int getID() {
		return -1;
	}

	@Override
	public String getName() {
		return "ICP sampling";
	}

}
