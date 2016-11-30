package de.unidue.ltl.gapfill.indexer;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class CorpusIndexer {

	private CollectionReaderDescription reader;
	private AnalysisEngineDescription preprocessing;
	
	public CorpusIndexer(File targetLocation, CollectionReaderDescription reader, AnalysisEngineDescription preprocessing) 
		throws Exception
	{
		this.reader = reader;
		this.preprocessing = preprocessing;
	}
	
	public void index()
		throws ResourceInitializationException, AnalysisEngineProcessException
	{
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(preprocessing);
		for (JCas jcas : new JCasIterable(reader)) {
			engine.process(jcas);
			
			int nrOfSentences = 0;
			for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
				indexSentence(jcas, sentence);
				
				nrOfSentences++;
			}
			System.out.println("Indexed " + nrOfSentences + " sentences.");
		}
	}
	
	private void indexSentence(JCas jcas, Sentence sentence) {
		// :)
	}
}