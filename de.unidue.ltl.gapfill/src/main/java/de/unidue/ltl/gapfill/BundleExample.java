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
import de.unidue.ltl.gapfill.util.DummySubstituteBuilder;
import de.unidue.ltl.gapfill.util.FastSubsConnector;
import de.unidue.ltl.gapfill.util.SubstituteVector;

public class BundleExample {

	public static void main(String[] args)
		throws Exception
	{
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				GUMReader.class,
				GUMReader.PARAM_SOURCE_LOCATION, "src/main/resources/corpora/GUM",
				GUMReader.PARAM_PATTERNS, "*.xml",
				GUMReader.PARAM_LANGUAGE, "en"
		);
		
		AnalysisEngineDescription preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
		
		Path indexPath = Paths.get("target/index");
		CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, preprocessing, new FastSubsConnector(), 10);
		indexer.index();
		//indexer.buildSubstitutes();

		
	}
}
