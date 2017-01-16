package de.unidue.ltl.gapfill.indexer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.gapfill.util.FastSubsConnector;

public class CorpusIndexer {

	public static final String SENTENCE_FILE_NAME = "sentences.txt";
	public static final String WORDS_FILE_NAME = "words.txt";
	public static final String DOCS_FILE_NAME = "docs.txt";
	public static final String SUBS_FILE_NAME = "subs.txt";
	
	private static final String TAB = "\t";
	
	private CollectionReaderDescription reader;
	private AnalysisEngineDescription preprocessing;
	
	private BufferedWriter sentenceWriter;	
	private BufferedWriter wordsWriter;	
	private BufferedWriter docsWriter;
	
	private Path docsFile;
	private Path subsFile;

	private FastSubsConnector fastsubs;
	
	private ConditionalFrequencyDistribution<String, Integer> word2sentence;
	
	public CorpusIndexer(Path targetLocation, CollectionReaderDescription reader, AnalysisEngineDescription preprocessing, int maxSubs) 
		throws Exception
	{
		this.reader = reader;
		this.preprocessing = preprocessing;
		
		if (Files.notExists(targetLocation)) {
			Files.createDirectories(targetLocation);			
		}
		
		this.docsFile = targetLocation.resolve(DOCS_FILE_NAME);
		this.subsFile = targetLocation.resolve(SUBS_FILE_NAME);
		
	    this.sentenceWriter = Files.newBufferedWriter(targetLocation.resolve(SENTENCE_FILE_NAME), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	    this.wordsWriter = Files.newBufferedWriter(targetLocation.resolve(WORDS_FILE_NAME), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	    this.docsWriter = Files.newBufferedWriter(docsFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	    
	    this.word2sentence = new ConditionalFrequencyDistribution<>();
	    
		String lmPath = "src/test/resources/lm/brown.lm";
	    fastsubs = new FastSubsConnector(maxSubs, lmPath);
	    fastsubs.initialize();
	}
	
	public void index()
		throws Exception
	{
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(preprocessing);
		Integer sentenceId = 0;
		for (JCas jcas : new JCasIterable(reader)) {
			engine.process(jcas);
			
			for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
				indexSentence(jcas, sentence, sentenceId);
				
				sentenceId++;
			}
			System.out.print(".");
		}
		
		writeWordToSentenceMap();		

		// relying on fastsubs EXIT token
		// currently no other way to break reading from the fastsubs process
		docsWriter.write(FastSubsConnector.EXIT_TOKEN);
		docsWriter.newLine();
		docsWriter.flush();
		docsWriter.close();
		
		sentenceWriter.flush();
		sentenceWriter.close();
		
		fastsubs.batchProcess(docsFile, subsFile);

		System.out.println();
		System.out.println("Indexed " + sentenceId + " sentences.");		
	}
		
	private void indexSentence(JCas jcas, Sentence sentence, Integer sentenceId)
		throws IOException
	{
		sentenceWriter.write(sentenceId.toString());
				
		for (Token token : JCasUtil.selectCovered(jcas, Token.class, sentence)) {
			docsWriter.write(token.getCoveredText());
			docsWriter.write(" ");
			
			sentenceWriter.write(TAB);
			sentenceWriter.write(token.getCoveredText());
			
			POS pos = token.getPos();
			if (pos instanceof N || pos instanceof V || pos instanceof ADJ) {
				String wordKey = getWordKey(token.getCoveredText(), pos.getPosValue());
				word2sentence.inc(wordKey,  sentenceId);				
			}
		}
		docsWriter.newLine();
		sentenceWriter.newLine();
	}
	
	private void writeWordToSentenceMap() throws IOException {
		for (String word : word2sentence.getConditions()) {
			// only write if a word appears in more than one sentence. No bundle with just one sentence.
			if (word2sentence.getFrequencyDistribution(word).getKeys().size() > 1) {
				wordsWriter.write(word);
				for (Integer id : word2sentence.getFrequencyDistribution(word).getKeys()) {
					wordsWriter.write(TAB);
					wordsWriter.write(id.toString());
				}
				wordsWriter.newLine();				
			}
		}
		wordsWriter.flush();
		wordsWriter.close();
	}
	
	private String getWordKey(String word, String pos) {
		return word + "_" + pos;
	}
}