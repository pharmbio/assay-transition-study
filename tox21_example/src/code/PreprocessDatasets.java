package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.csv.CSVFormat;

import com.arosbio.auth.InvalidLicenseException;
import com.arosbio.chem.io.in.CSVChemFileReader;
import com.arosbio.io.StreamUtilsCPSign;
import com.arosbio.modeling.CPSignFactory;
import com.arosbio.modeling.cheminf.ChemicalProblem;
import com.arosbio.modeling.cheminf.ChemicalProblem.ParseInfo;
import com.arosbio.modeling.cheminf.NamedLabels;
import com.arosbio.modeling.cheminf.descriptors.SignaturesDescriptor;
import com.arosbio.modeling.data.Problem.RecordType;
import com.arosbio.modeling.data.transform.duplicates.InterDatasetDuplicatesResolver;
import com.arosbio.modeling.data.transform.duplicates.UseVoting;
import com.arosbio.modeling.data.transform.duplicates.KeepFirstRecord;

public class PreprocessDatasets {

	// fill in these values
	private static final String LICENSE_PATH = "";
	private static final String ASSAY_OLD_PATH = "TODO/resources/sr-are.smiles.gz";
	private static final String ASSAY_NEW_PATH = "TODO/resources/tox21_score.smiles.gz";
	private static final String ENDPOINT = "SR-ARE";
	private static final String TRAIN_OUTPUT_LIBSVM = "TODO/resources/train.svm.gz";
	private static final String SCORE_OUTPUT_LIBSVM = "TODO/resources/score.svm.gz";


	public static void main(String[] args) throws InvalidLicenseException, IOException {

		// Instantiate the factory to validate your credentials found in the license
		new CPSignFactory(new File(LICENSE_PATH).toURI());

		// use signatures descriptor using height 1 to 3
		ChemicalProblem problem = new ChemicalProblem(Arrays.asList(new SignaturesDescriptor(1,3))); 
		problem.initializeDescriptors();

		// "Old" records (tox21 training records) stored in the "Modeling exclusive" Dataset
		try (
				InputStream istream = StreamUtilsCPSign.unZIP(new FileInputStream(ASSAY_OLD_PATH));
				Reader fileReader = new BufferedReader(new InputStreamReader(istream));
				CSVChemFileReader reader = new CSVChemFileReader(CSVFormat.DEFAULT.withDelimiter('\t').withHeader("SMILES","ID",ENDPOINT), fileReader)){
			ParseInfo info = problem.fromMolsIterator(reader, ENDPOINT, new NamedLabels(Arrays.asList("0","1")),RecordType.MODELING_EXCLUSIVE);
			System.out.println(info);
		}
		
		
		new UseVoting().transform(problem.getDataset(RecordType.MODELING_EXCLUSIVE));
		System.out.println("Old assay after duplicates filtering: " + problem.getDataset(RecordType.MODELING_EXCLUSIVE).size() + ", freq: " + problem.getDataset(RecordType.MODELING_EXCLUSIVE).getLabelFrequencies());
		
		

		// "New" records (tox21 score) scored in the "normal" Dataset
		try (
				InputStream istream = StreamUtilsCPSign.unZIP(new FileInputStream(ASSAY_NEW_PATH));
				Reader fileReader = new BufferedReader(new InputStreamReader(istream));
				CSVChemFileReader reader = new CSVChemFileReader(
						CSVFormat.DEFAULT
						.withDelimiter('\t')
						.withCommentMarker(null)
						.withFirstRecordAsHeader(), 
						fileReader)){
			ParseInfo info = problem.fromMolsIterator(reader, ENDPOINT, new NamedLabels(Arrays.asList("0","1")),RecordType.NORMAL);
			System.out.println(info);
		}
		
		new UseVoting().transform(problem.getDataset(RecordType.NORMAL));
		System.out.println("New assay after duplicates filtering: " + problem.getDataset(RecordType.NORMAL).size() + ", freq: " + problem.getDataset(RecordType.NORMAL).getLabelFrequencies());
		
		new InterDatasetDuplicatesResolver(new KeepFirstRecord()).transform(problem.getDataset(), problem.getDataset(RecordType.MODELING_EXCLUSIVE));
		System.out.println("Old assays after inter-dataset-filtering: " + problem.getDataset(RecordType.MODELING_EXCLUSIVE).size() + ", freq: " + problem.getDataset(RecordType.MODELING_EXCLUSIVE).getLabelFrequencies());
		
		// Write the records in LIBSVM format
		problem.getDataset(RecordType.MODELING_EXCLUSIVE).writeRecords(new FileOutputStream(TRAIN_OUTPUT_LIBSVM), true);
		problem.getDataset(RecordType.NORMAL).writeRecords(new FileOutputStream(SCORE_OUTPUT_LIBSVM), true);
	}

}
