package de.unidue.ltl.gapfill;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;

import de.unidue.ltl.gapfill.indexer.CorpusIndexer;
import de.unidue.ltl.gapfill.indexer.SubstituteRetriever;
import de.unidue.ltl.gapfill.io.GUMReader;
import de.unidue.ltl.gapfill.util.SubstituteVector;

public class BundleExample {

	public static void main(String[] args)
		throws Exception
	{
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				GUMReader.class,
				GUMReader.PARAM_SOURCE_LOCATION, "src/main/resources/corpora/GUM",
				GUMReader.PARAM_PATTERNS, "GUM_news_asylum.xml",
				GUMReader.PARAM_LANGUAGE, "en"
		);
		
		AnalysisEngineDescription preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
		
		Path indexPath = Paths.get("target/index");
		CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, preprocessing, 100);
		indexer.index();
		
		SubstituteRetriever retriever = new SubstituteRetriever(indexPath);
		for (SubstituteVector sv : retriever.getSubstituteVectors("boat")) {
			System.out.println(sv.getSentenceId() + ":" + sv.getTokenOffset());
			System.out.println(sv);
		}
		
//		FastSubsConnector fastsubs = new FastSubsConnector(10, "src/test/resources/lm/brown.lm");
//		fastsubs.initialize();
//	
//		List<SubstituteVector> subs = fastsubs.getSubstitutes("This is an example");

		
	}
}
