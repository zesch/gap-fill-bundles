package de.unidue.ltl.gapfill.indexer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.unidue.ltl.gapfill.util.FastSubsConnector;
import de.unidue.ltl.gapfill.util.SubstituteVector;

public class SubstituteRetriever {
	
	private Path index;
	private int maxSubs;
	
	public SubstituteRetriever(Path index) {
		super();
		this.index = index;
		this.maxSubs = Integer.MAX_VALUE;
	}
	
	public SubstituteRetriever(Path index, int maxSubs) {
		super();
		this.index = index;
		this.maxSubs = maxSubs;
	}

	public List<SubstituteVector> getSubstituteVectors(String targetWord) 
		throws Exception
	{
		List<SubstituteVector> subs = new ArrayList<>();
		
		String pattern = targetWord + "\t";
		
		int sentenceId = 0;
		int tokenOffset = 0;
		for (String line : getSubstitutes(index)) {
			if (line.startsWith("</s>")) {
				sentenceId++;
				tokenOffset = 0;
			}
			if (line.startsWith(pattern)) {
				SubstituteVector sv = FastSubsConnector.fastsubs2vector(line, maxSubs);
				sv.setSentenceId(sentenceId);
				subs.add(sv);
			}
			tokenOffset++;
		}
		return subs;
	}
	
	private Collection<String> getSubstitutes(Path index) 
			throws IOException
	{
		Path subsFile = index.resolve(CorpusIndexer.SUBS_FILE_NAME);
		return FileUtils.readLines(subsFile.toFile());
	}
	
	public Path getIndex() {
		return index;
	}

	public void setIndex(Path index) {
		this.index = index;
	}
}
