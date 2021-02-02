package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import com.arosbio.modeling.CPSignFactory;
import com.arosbio.modeling.data.DataRecord;
import com.arosbio.modeling.data.DataUtils;
import com.arosbio.modeling.data.Dataset;

public class Utils {

	public static void verifyCPSign() throws Exception {
		new CPSignFactory(Utils.class.getResource("/resources/cpsign.license").toURI());
	}
	
	public static final char RESULTS_CSV_DELIM = ',';
	
	public static <T> Set<T> intersection(Collection<T> c1, Collection<T> c2) {
        Set<T> intersect = new HashSet<T>(c1);
        
        intersect.retainAll(c2);
        
        return intersect;
    }

	public static double pIC50(double uM_IC50) {
		return -1*Math.log10(1e-6*uM_IC50);
	}
	
	public static double uM(double pIC50) {
		return Math.pow(10, -1*pIC50)*1e6;
	}
	
	public static Dataset subsample(Dataset input, long seed, int size) {
		Dataset clone = input.clone();
		List<DataRecord> recs = clone.getRecords();
		Collections.shuffle(recs, new Random(seed));
		
		if (size < 0)
			return clone;
		
		if (size<= recs.size())
			clone.setRecords(recs.subList(0, size));
		if (clone.size() != size)
			throw new RuntimeException("Issue when subsampling dataset: " + clone.size() + " vs " + size);
		return clone;
	}
	
	public static void countNumOfEachSignature(Dataset d) {
		int numFeats = d.getNumFeatures();
		
		Map<Integer, Integer> featureCounts = new HashMap<>();
		
		for (int i=0; i<numFeats; i++) {
			List<Double> feat = DataUtils.extractColumn(d.getRecords(), i);
			int count = count(feat, new Predicate<Double>() {
				@Override
				public boolean test(Double t) {
					return t != 0;
				}
			});
			
			featureCounts.put(count, featureCounts.getOrDefault(count, 0) + 1);
		}
		
		List<Integer> occurs = new ArrayList<>(featureCounts.keySet());
		Collections.sort(occurs);
		
		for (int i=0; i<occurs.size() && i <20; i++) {
			System.out.println("" + occurs.get(i) + ": " + featureCounts.get(occurs.get(i)));
		}
		
	}
	
	public static double sum(Collection<Double> c) {
		double sum = 0;
		for (Double d : c) {
			sum+= d;
		}
		return sum;
	}
	
	public static int count(Collection<Double> c, Predicate<Double> p) {
		int count = 0;
		for (Double d : c) {
			if (p.test(d)) {
				count++;
			}
		}
		return count;
	}
}
