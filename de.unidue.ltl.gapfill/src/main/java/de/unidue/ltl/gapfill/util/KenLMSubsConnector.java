package de.unidue.ltl.gapfill.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class KenLMSubsConnector implements SubstituteBuilder {
	
	public static final String SENTENCE_FILE_NAME = "sentences.txt";
	public static final String WORDS_FILE_NAME = "words.txt";
	public static final String DOCS_FILE_NAME = "docs.txt";
	public static final String SUBS_FILE_NAME = "subs.txt";
	
	public static final String TAB = "\t";

	Path languageModel;
	Path inputFile;
	Path outputFile;
	FrequencyDistribution<String> fd;
	int nrOfSubs;
	
	public KenLMSubsConnector(Path indexPath, Path languageModel, int nrOfSubs, CollectionReaderDescription reader, AnalysisEngineDescription preprocessing){
		this.inputFile = indexPath.resolve(SENTENCE_FILE_NAME);
		this.outputFile = indexPath.resolve(SUBS_FILE_NAME);
		this.languageModel = languageModel;
		this.nrOfSubs = nrOfSubs;
		try{
			buildFD(reader, preprocessing);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void buildSubstitutes() throws Exception {


		List<String> sentences = FileUtils.readLines(inputFile.toFile());
		for(String sentence : sentences){
			String[] split = sentence.split(TAB);
			for(int i = 1; i < split.length; i++){;
				String subsVector = buildSubstituteVector(split, i);
			}
			
		}
		
		for(String s : fd.getMostFrequentSamples(nrOfSubs)){
			System.out.println(s);
		}
	}
	
	private String buildSubstituteVector(String[] words, int targetPosition) throws Exception{

		
		
		List<Substitute> subsList = new ArrayList<>();
		for(String sub : fd.getMostFrequentSamples(1000)){
	
			words[targetPosition] = sub;
			
			double value = 0;
			//TODO: Get value from jweb1t
			Substitute substitute = new Substitute(sub, value);
			subsList.add(substitute);
		}
		
		Collections.sort(subsList);
		String v = "";
		for(Substitute s : subsList){
			v += s.getValue() + "   ";
		}
		System.out.println(v);
		return null;
	}

	private String createSentence(String[] words, int targetPosition, String targetWord){

		//start at 1 because position 0 contains the sentence id
		String sentence = "";
		for(int i = 1; i < words.length; i++){
			if(i == targetPosition){
				sentence += targetWord;
			} else {
				sentence += words[i];
			}
			sentence += " ";
		}
		sentence = sentence.substring(0, sentence.length() - 1);
		return sentence;
	}
	
	private void buildFD(CollectionReaderDescription reader, AnalysisEngineDescription preprocessing) throws Exception{
		AnalysisEngine engine = AnalysisEngineFactory.createEngine(preprocessing);
		fd = new FrequencyDistribution<>();
		for (JCas jcas : new JCasIterable(reader)) {
			engine.process(jcas);
			for (Token token : JCasUtil.select(jcas, Token.class)) {
				fd.inc(token.getCoveredText());
			}
		}
		System.out.println("---Built fd---");
	}
}
