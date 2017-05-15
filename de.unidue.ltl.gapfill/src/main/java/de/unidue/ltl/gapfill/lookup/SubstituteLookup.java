package de.unidue.ltl.gapfill.lookup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.*;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.unidue.ltl.gapfill.util.FastSubsConnector;
import de.unidue.ltl.gapfill.util.SubstituteVector;
import de.unidue.ltl.gapfill.util.SubstituteVectorUtil; 



public class SubstituteLookup {
	
	public static final String SENTENCE_FILE_NAME = "sentences.txt";
	public static final String WORDS_FILE_NAME = "words.txt";
	public static final String SUBS_FILE_NAME = "subs.txt";

	private static final String TAB = "\t";
	
	private BufferedReader wordsReader;	
	private BufferedReader subsReader;
	
	private Path targetLocation;
	
	private List<String> sentences;
	
	private int maxSubs;
	
	public SubstituteLookup(Path targetLocation, int maxSubs) throws Exception{
		this.maxSubs = maxSubs;
		this.targetLocation = targetLocation;
		if (Files.notExists(targetLocation)) {
			Files.createDirectories(targetLocation);			
		}
		//this.wordsReader = Files.newBufferedReader(targetLocation.resolve(WORDS_FILE_NAME));
		sentences = FileUtils.readLines(targetLocation.resolve(SENTENCE_FILE_NAME).toFile());
	}
	
	

	private String getSentence(int sentenceId) throws IOException{
		String[] sentence = sentences.get(sentenceId).split(TAB, 2);
		return sentence[1];
	}
	
	public List<SubstituteVector> getBundle(int size, String token, String pos) throws Exception{
		List<SubstituteVector> substituteVectors = getSubstituteVectors(token, pos);
		SubstituteVector bundleVector = substituteVectors.get(0);
		substituteVectors.remove(bundleVector);
		
		
		List<SubstituteVector> result = SubstituteVectorUtil.getBundle(size, bundleVector, substituteVectors);
		
		for(SubstituteVector sv : result){
			System.out.println(sv.getSentenceWithGap());
		}
		
		return result;
	}
	
	public List<SubstituteVector> getSubstituteVectors(String token, String pos) throws Exception{
		this.wordsReader = Files.newBufferedReader(targetLocation.resolve(WORDS_FILE_NAME));
		List<SubstituteVector> substituteVectors = new ArrayList<>();
		
		String line;
		while((line = wordsReader.readLine()) != null){
			if(line.startsWith(token + "_" + pos))
				break;
		}
		
		if(line == null)
			return null;
		
		String[] split = line.split(TAB);
		for(int i = 1; i < split.length; i++){
			String occurence = split[i];
			String[] sentenceTokenPair = occurence.split("_");
			int sentenceId = Integer.parseInt(sentenceTokenPair[0]);
			int tokenId = Integer.parseInt(sentenceTokenPair[1]);
			substituteVectors.add(getSubstituteVector(sentenceId, tokenId));
		}
		
		return substituteVectors;
	}
	
	private SubstituteVector getSubstituteVector(int sentenceId, int tokenId) throws IOException{
		this.subsReader = Files.newBufferedReader(targetLocation.resolve(SUBS_FILE_NAME));
		String line;
		int currentSentence = 0;
		int currentToken = 0;
		SubstituteVector sub = null;
		
		while((line = subsReader.readLine()) != null){
			
			if(currentSentence == sentenceId && currentToken == tokenId){
				sub = FastSubsConnector.fastsubs2vector(line, maxSubs);
				sub.setSentence(getSentence(sentenceId));
				sub.setTokenId(tokenId);
			}
			currentToken++;
			
			if(line.startsWith("</s>")){
				currentSentence++;
				currentToken = 0;
			}
		}
		return sub;
	}

}
