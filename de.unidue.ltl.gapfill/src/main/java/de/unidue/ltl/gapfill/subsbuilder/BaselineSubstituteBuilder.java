package de.unidue.ltl.gapfill.subsbuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.gapfill.util.Substitute;

public class BaselineSubstituteBuilder implements SubstituteBuilder {
	
	public static final String SENTENCE_FILE_NAME = "sentences.txt";
	public static final String WORDS_FILE_NAME = "words.txt";
	public static final String DOCS_FILE_NAME = "docs.txt";
	public static final String SUBS_FILE_NAME = "subs.txt";
	public static final String EXIT_TOKEN = "EXIT_TOKEN";
	
	private Path docsFile;
	private Path subsFile;
	private FrequencyDistribution<String> fd;
	private int nrOfSubs;
	private boolean weightedProbabilities;
	
	private String TAB = "\t";
	

	public BaselineSubstituteBuilder(Path indexPath,
			int nrOfSubs, 
			boolean weightedProbabilities){
		this.docsFile = indexPath.resolve(DOCS_FILE_NAME);
		this.subsFile = indexPath.resolve(SUBS_FILE_NAME);
		this.weightedProbabilities = weightedProbabilities;
		this.nrOfSubs = nrOfSubs;
		buildFD();
	}

	@Override
	public void buildSubstitutes() throws Exception {
		if(subsFile.toFile().delete())
			subsFile.toFile().createNewFile();
		
		BufferedReader br = Files.newBufferedReader(docsFile);
		BufferedWriter bw = Files.newBufferedWriter(subsFile);
		String inputline;
		while((inputline = br.readLine())!=null){
			if(inputline.equals("EXIT_TOKEN"))
				break;
			String[] words = inputline.split(" ");
			
			for(String word : words){
				String line = word + getRandomSubstituteVector();
				bw.write(line);
				bw.newLine();
			}
			String line = "</s>" + getRandomSubstituteVector();
			bw.write(line);
			bw.newLine();
			
		}
		bw.flush();
	}
	
	@SuppressWarnings("unchecked")
	private String getRandomSubstituteVector(){
		List<String> frequentWords = fd.getMostFrequentSamples(100);
		
		StringBuilder sb = new StringBuilder();
		float minX = 0.0f;
		float maxX = 1.0f;

		Random rand = new Random();
		List<Substitute> substitutes = new ArrayList<>();

		
		for(int i = 0; i < nrOfSubs; i++){
			int randomInt = rand.nextInt(frequentWords.size());
			String target = frequentWords.get(randomInt);
			float targetvalue = (rand.nextFloat() * (maxX - minX) + minX);
			if(weightedProbabilities){
				targetvalue = targetvalue * ((float)fd.getCount(target)/fd.getN());
			}
			
			Substitute substitute = new Substitute(target,targetvalue);
			substitutes.add(substitute);

			frequentWords.remove(randomInt);
			
		}
		Collections.sort(substitutes);
		
		for(Substitute substitute : substitutes){
			sb.append(TAB);
			sb.append(substitute.getName());
			sb.append(TAB +"-");
			sb.append(substitute.getValue());
		}
		return sb.toString();
	}
	
	private void buildFD() {
		fd = new FrequencyDistribution<>();
		try(BufferedReader br = Files.newBufferedReader(docsFile);){
			String line;
			while((line = br.readLine())!=null){
				if(line.equals(EXIT_TOKEN))
					break;
				String[] words = line.split(" ");
				for(String word : words){
					fd.inc(word);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}
