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

public class BundleExample
{
    static int LIMIT=25;

    public static void main(String[] args) throws Exception
    {
        Properties p = new Properties();
        FileInputStream f = new FileInputStream(args[0]);
        p.load(f);
        f.close();
        
        String word = args[1];
        String pos = args[2];

        String sourceFolder = p.getProperty("folder");
        String model = p.getProperty("model");
        String indexLocation = p.getProperty("index");

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_SOURCE_LOCATION, sourceFolder,
                LineTokenTagReader.PARAM_PATTERNS, "*.txt", LineTokenTagReader.PARAM_LANGUAGE,
                "de");

        AnalysisEngineDescription preprocessing = AnalysisEngineFactory
                .createEngineDescription(NoOpAnnotator.class);
        Path lmPath = Paths.get(model);
        Path indexPath = Paths.get(indexLocation);

        CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, preprocessing, LIMIT);
        indexer.index();

        System.out.println("Calling FastSubs to build substitutes---");
         FastSubsConnector subsBuilder = new FastSubsConnector(indexPath, lmPath, LIMIT);
         subsBuilder.buildSubstitutes();
         System.out.println("--- done");

        System.out.println("Creating bundle ---");
        SubstituteLookup sl = new SubstituteLookup(indexPath, LIMIT);
        List<SubstituteVector> bundle = sl.getBundle(4, word, pos);
        for (SubstituteVector s : bundle) {
            System.out.println(s);
        }
        System.out.println("--- done");

    }
}
