package de.unidue.ltl.gapfill.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DummySubstituteBuilder implements SubstituteBuilder {
	
	private Path inputFile;
	private Path outputFile;
	private Path languageModel;
	private int nrOfSubs;
	
	private String TAB = "\t";
	
	private String[] subs = {"The","it","bla","schubidu","asdf"};

	@Override
	public void initialize(Path inputFile, Path outputFile, Path languageModel, int nrOfSubs) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.languageModel = languageModel;
		
		if(nrOfSubs > subs.length){
			System.out.println("More subs requested than possible. Nr of Subs will be set to " + subs.length);
			this.nrOfSubs = subs.length;
		} else {
			this.nrOfSubs = nrOfSubs;
		}
		
	}

	@Override
	public void buildSubstitutes() throws Exception {
		BufferedReader br = Files.newBufferedReader(inputFile);
		BufferedWriter bw = Files.newBufferedWriter(outputFile);
		String inputline;
		
		while((inputline = br.readLine())!=null){
			String[] words = inputline.split(" ");
			for(String word : words){
				String line = word + getRandomSubs();
				bw.write(line);
				bw.newLine();
			}
			String line = "</s>" + getRandomSubs();
			bw.write(line);
			bw.newLine();
			
		}
		bw.flush();
	}
	
	private String getRandomSubs(){
		StringBuilder sb = new StringBuilder();
		List<String> subsList = new ArrayList<>(Arrays.asList(subs));
		float minX = 2.0f;
		float maxX = 17.0f;

		Random rand = new Random();

		float randomvalue = (rand.nextFloat() * (maxX - minX) + minX);
		for(int i = 0; i < nrOfSubs; i++){
			int randomInt = rand.nextInt(subsList.size());
			
			sb.append(TAB);
			sb.append(subsList.get(randomInt));
			sb.append(TAB +"-");
			sb.append(randomvalue);

			subsList.remove(randomInt);
			randomvalue = (rand.nextFloat() * (randomvalue - minX) + minX);
			
		}
		return sb.toString();
	}

}
