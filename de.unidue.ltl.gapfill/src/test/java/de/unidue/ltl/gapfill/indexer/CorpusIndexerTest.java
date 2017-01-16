package de.unidue.ltl.gapfill.indexer;

import java.nio.file.Paths;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.junit.Test;

import de.unidue.ltl.gapfill.io.GUMReader;

public class CorpusIndexerTest {

	@Test
	public void corpusIndexerTest() 
		throws Exception
	{
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				GUMReader.class,
				GUMReader.PARAM_SOURCE_LOCATION, "src/main/resources/corpora/GUM",
				GUMReader.PARAM_PATTERNS, "GUM_news_asylum.xml",
				GUMReader.PARAM_LANGUAGE, "en"
		);
		
		AnalysisEngineDescription preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
		
		CorpusIndexer indexer = new CorpusIndexer(Paths.get("target/index"), reader, preprocessing, 10);
		indexer.index();
	}
}
