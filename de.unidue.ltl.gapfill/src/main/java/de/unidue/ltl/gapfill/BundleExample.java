package de.unidue.ltl.gapfill;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;

import de.unidue.ltl.gapfill.indexer.CorpusIndexer;
import de.unidue.ltl.gapfill.io.GUMReader;
import de.unidue.ltl.gapfill.lookup.SubstituteLookup;
import de.unidue.ltl.gapfill.subsbuilder.BaselineSubstituteBuilder;
import de.unidue.ltl.gapfill.subsbuilder.FastSubsConnector;
import de.unidue.ltl.gapfill.subsbuilder.JWeb1TSubsBuilder;
import de.unidue.ltl.gapfill.subsbuilder.SubstituteBuilder;
import de.unidue.ltl.gapfill.util.SubstituteVector;

public class BundleExample {
	
	public static void main(String[] args)
		throws Exception
	{
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				GUMReader.class,
				GUMReader.PARAM_SOURCE_LOCATION, "src/main/resources/corpora/GUM",
				GUMReader.PARAM_PATTERNS, "GUM_interview_ants.xml",
				GUMReader.PARAM_LANGUAGE, "en"
		);
		
		AnalysisEngineDescription preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
		Path lmPath = Paths.get("src/test/resources/lm/brown.lm");
		Path indexPath = Paths.get("target/index");

		
		//BaselineSubstituteBuilder subsBuilder = new BaselineSubstituteBuilder(indexPath, 100, true);
		//FastSubsConnector subsBuilder = new FastSubsConnector(indexPath, lmPath, 100);
		//CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, preprocessing,100);
		//indexer.index();
		JWeb1TSubsBuilder s = new JWeb1TSubsBuilder(indexPath, 100);
		
		
		
		//BaselineSubstituteBuilder subsBuilder = new BaselineSubstituteBuilder(indexPath, 100, true);
		//subsBuilder.buildSubstitutes();
//		SubstituteLookup sl = new SubstituteLookup(indexPath, 100);
//		sl.getBundle(4, "find", "VV");
		
	}
}
