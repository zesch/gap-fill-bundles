package de.unidue.ltl.gapfill.indexer;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.unidue.ltl.gapfill.io.GUMReader;

public class CorpusIndexerTest {

	@Test
	public void corpusIndexerTest() 
		throws Exception
	{
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				GUMReader.class,
				GUMReader.PARAM_SOURCE_LOCATION, "src/main/resources/corpora/GUM",
				GUMReader.PARAM_PATTERNS, ".xml"
		);
		
		AnalysisEngineDescription preprocessing = AnalysisEngineFactory.createEngineDescription(
				AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class),
				AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_VARIANT, "maxent")
		);
		
		CorpusIndexer indexer = new CorpusIndexer(new File("target/index"), reader, preprocessing);
		indexer.index();
	}
}
