package de.unidue.ltl.gapfill;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;

import de.unidue.ltl.gapfill.indexer.CorpusIndexer;
import de.unidue.ltl.gapfill.io.LineTokenTagReader;
import de.unidue.ltl.gapfill.lookup.SubstituteLookup;
import de.unidue.ltl.gapfill.subsbuilder.FastSubsConnector;
import de.unidue.ltl.gapfill.util.SubstituteVector;

public class BundleExample {
	
	public static void main(String[] args)
		throws Exception
	{
	    Properties p = new Properties();
        FileInputStream f = new FileInputStream("src/main/resources/config.txt");
        p.load(f);
        f.close();
        
        String sourceFolder = p.getProperty("folder");
        String model = p.getProperty("model");
        String indexLocation = p.getProperty("index");
	    
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
				LineTokenTagReader.class,
				LineTokenTagReader.PARAM_SOURCE_LOCATION, sourceFolder,
				LineTokenTagReader.PARAM_PATTERNS, "*.txt",
				LineTokenTagReader.PARAM_LANGUAGE, "de"
		);
		
		AnalysisEngineDescription preprocessing = AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class);
		Path lmPath = Paths.get(model);
		Path indexPath = Paths.get(indexLocation);

	    CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, preprocessing,100);
        indexer.index();
		
		//BaselineSubstituteBuilder subsBuilder = new BaselineSubstituteBuilder(indexPath, 100, true);
		FastSubsConnector subsBuilder = new FastSubsConnector(indexPath, lmPath, 100);

//		JWeb1TSubsBuilder s = new JWeb1TSubsBuilder(indexPath, 100);
		subsBuilder.buildSubstitutes();
		List<SubstituteVector> substitutes = subsBuilder.getSubstitutes("Hallo , heute ist das Wetter besonders gut ");
		
      SubstituteLookup sl = new SubstituteLookup(indexPath, 100);
      List<SubstituteVector> bundle = sl.getBundle(4, "bevorstehend", "ADJA");
      for(SubstituteVector s : bundle) {
          System.out.println(s);
      }
		
		
		//BaselineSubstituteBuilder subsBuilder = new BaselineSubstituteBuilder(indexPath, 100, true);
		//subsBuilder.buildSubstitutes();

		
	}
}
