package de.unidue.ltl.gapfill;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;

import de.unidue.ltl.gapfill.indexer.CorpusIndexer;
import de.unidue.ltl.gapfill.io.LineTokenTagReader;
import de.unidue.ltl.gapfill.subsbuilder.FastSubsConnector;

public class BundleExample {
	
	public static void main(String[] args)
		throws Exception
	{
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				LineTokenTagReader.class,
				LineTokenTagReader.PARAM_SOURCE_LOCATION, "src/main/resources/corpora/HamburgDepTreebank",
				LineTokenTagReader.PARAM_PATTERNS, "*.txt",
				LineTokenTagReader.PARAM_LANGUAGE, "de"
		);
		
		AnalysisEngineDescription preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
		Path lmPath = Paths.get("src/main/resources/gerModel.arpa");
		Path indexPath = Paths.get("target/index");

	    CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, preprocessing,100);
        indexer.index();
		
		//BaselineSubstituteBuilder subsBuilder = new BaselineSubstituteBuilder(indexPath, 100, true);
		FastSubsConnector subsBuilder = new FastSubsConnector(indexPath, lmPath, 100);

//		JWeb1TSubsBuilder s = new JWeb1TSubsBuilder(indexPath, 100);
		subsBuilder.buildSubstitutes();
		subsBuilder.getSubstitutes("Hallo du da");
		
		
		//BaselineSubstituteBuilder subsBuilder = new BaselineSubstituteBuilder(indexPath, 100, true);
		//subsBuilder.buildSubstitutes();
//		SubstituteLookup sl = new SubstituteLookup(indexPath, 100);
//		sl.getBundle(4, "find", "VV");
		
	}
}
