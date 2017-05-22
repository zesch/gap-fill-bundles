package de.unidue.ltl.gapfill.util;

import java.nio.file.Path;

public interface SubstituteBuilder {
	
	void initialize(Path inputFile, Path outputFile, Path languageModel, int nrOfSubs);
	
	void buildSubstitutes() throws Exception;

}
