package de.unidue.ltl.gapfill.indexer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import de.unidue.ltl.gapfill.util.BaselineSubstituteBuilder;
import de.unidue.ltl.gapfill.util.FastSubsConnector;
import de.unidue.ltl.gapfill.util.SubstituteBuilder;

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
	private Path wordsFile;
	private Path sentenceFile;

	private SubstituteBuilder substituteBuilder;
	
	private ConditionalFrequencyDistribution<String, String> word2sentence;
	
	public CorpusIndexer(Path indexPath, 
			CollectionReaderDescription reader, 
			AnalysisEngineDescription preprocessing,
			SubstituteBuilder substituteBuilder,
			int maxSubs) 
		throws Exception
	{
		this.reader = reader;
		this.preprocessing = preprocessing;
		this.substituteBuilder = substituteBuilder;
		
		if (Files.notExists(indexPath)) {
			Files.createDirectories(indexPath);			
		}
		
		this.docsFile = indexPath.resolve(DOCS_FILE_NAME);
		this.subsFile = indexPath.resolve(SUBS_FILE_NAME);
		this.wordsFile = indexPath.resolve(WORDS_FILE_NAME);
		this.sentenceFile = indexPath.resolve(SENTENCE_FILE_NAME);
		

	    
	    this.word2sentence = new ConditionalFrequencyDistribution<>();

		Path lmPath = Paths.get("src/test/resources/lm/brown.lm");
	}
	
	public void index()
		throws Exception
	{
		clearFiles();
		
	    this.sentenceWriter = Files.newBufferedWriter(sentenceFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	    this.wordsWriter = Files.newBufferedWriter(wordsFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
	    this.docsWriter = Files.newBufferedWriter(docsFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		
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
		

		System.out.println();
		System.out.println("Indexed " + sentenceId + " sentences.");		
	}
	
	public void buildSubstitutes() throws Exception{
		if(subsFile.toFile().delete())
			subsFile.toFile().createNewFile();
		substituteBuilder.buildSubstitutes();
		System.out.println("Substitutes build.");
	}
		
	private void indexSentence(JCas jcas, Sentence sentence, Integer sentenceId)
		throws IOException
	{
		sentenceWriter.write(sentenceId.toString());
		int tokenPosition = 0;
		for (Token token : JCasUtil.selectCovered(jcas, Token.class, sentence)) {
			
			docsWriter.write(token.getCoveredText());
			docsWriter.write(" ");
			
			sentenceWriter.write(TAB);
			sentenceWriter.write(token.getCoveredText());
			
			POS pos = token.getPos();

			if (pos instanceof N || pos.getPosValue().startsWith("V")|| pos instanceof ADJ) {
				String wordKey = getWordKey(token.getCoveredText(), pos.getPosValue());
				String wordPosition = getWordPosition(sentenceId, tokenPosition);
				word2sentence.inc(wordKey,  wordPosition );				
			}
			tokenPosition++;
		}
		docsWriter.newLine();
		sentenceWriter.newLine();
	}
	
	private void writeWordToSentenceMap() throws IOException {
		for (String word : word2sentence.getConditions()) {
			// only write if a word appears in more than one sentence. No bundle with just one sentence.
			if (word2sentence.getFrequencyDistribution(word).getKeys().size() > 1) {
				wordsWriter.write(word);
				for (String id : word2sentence.getFrequencyDistribution(word).getKeys()) {
					wordsWriter.write(TAB);
					wordsWriter.write(id.toString());
				}
				wordsWriter.newLine();				
			}
		}
		wordsWriter.flush();
		wordsWriter.close();
	}
	
	private void clearFiles() throws Exception{
		if(docsFile.toFile().delete())
			docsFile.toFile().createNewFile();

		if(wordsFile.toFile().delete())
			wordsFile.toFile().createNewFile();
		if(sentenceFile.toFile().delete())
			sentenceFile.toFile().createNewFile();
	}
	
	private String getWordKey(String word, String pos) {
		return word + "_" + pos;
	}
	
	private String getWordPosition(int sentenceId, int tokenOffset){
		return sentenceId + "_" + tokenOffset;
	}
}