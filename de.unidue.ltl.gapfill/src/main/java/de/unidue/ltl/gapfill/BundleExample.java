package de.unidue.ltl.gapfill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
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
    static int LIMIT = 25;

    public static void main(String[] args) throws Exception
    {
        Properties p = new Properties();
        FileInputStream f = new FileInputStream(args[0]);
        p.load(f);
        f.close();

        for (int i = 1; i < args.length; i += 2) {
            String word = args[i];
            String pos = args[i + 1];

            System.out.println(word + " " + pos);

            String sourceFolder = p.getProperty("folder");
            String model = p.getProperty("model");
            String indexLocation = p.getProperty("index");
            String outputFolder = p.getProperty("output");
            int MAX_SENT_LEN = Integer.parseInt(p.getProperty("sentLen"));

            CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                    LineTokenTagReader.class, LineTokenTagReader.PARAM_SOURCE_LOCATION,
                    sourceFolder, LineTokenTagReader.PARAM_PATTERNS, "*.txt",
                    LineTokenTagReader.PARAM_LANGUAGE, "de");

            Path indexPath = Paths.get(indexLocation);

            if (rebuildIndex(indexLocation, sourceFolder+"_maxLen_" + MAX_SENT_LEN)) {
                System.out.println("Building index information");

                AnalysisEngineDescription preprocessing = AnalysisEngineFactory
                        .createEngineDescription(NoOpAnnotator.class);
                Path lmPath = Paths.get(model);

                CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, preprocessing, LIMIT, MAX_SENT_LEN);
                indexer.index();

                System.out.println(" --- retrieving substitutes");
                FastSubsConnector subsBuilder = new FastSubsConnector(indexPath, lmPath, LIMIT);
                subsBuilder.buildSubstitutes();
            }
            else {
                System.out.println(
                        "Use existing index from previous runs at [" + indexLocation + "]");
            }

            System.out.println(" --- creating bundles");
            SubstituteLookup sl = new SubstituteLookup(indexPath, LIMIT);
            List<SubstituteVector> bundle = sl.getBundle(4, word, pos);
            
            if(bundle.isEmpty()) {
                continue;
            }
            
            StringBuilder sb = new StringBuilder();
            for (SubstituteVector s : bundle) {
                sb.append(s.getSentenceWithGap() + "\n");
                sb.append("\n");
            }
            FileUtils.writeStringToFile(
                    new File(outputFolder, pos + "_" + word + "_" + "bundleResult.txt"),
                    sb.toString());
        }
    }

    private static boolean rebuildIndex(String index, String source) throws Exception
    {
        File file = new File(index + "/id.txt");
        if (!file.exists()) {
            writeIdFile(file, source);
            return true;
        }

        Properties p = new Properties();
        FileInputStream f = new FileInputStream(file.getAbsolutePath());
        p.load(f);
        f.close();

        String property = p.getProperty("source");
        if (property == null) {
            writeIdFile(file, source);
            return true;
        }

        if (!property.equals(source)) {
            writeIdFile(file, source);
            return true;
        }

        return false;
    }

    private static void writeIdFile(File file, String source) throws Exception
    {
        Properties p = new Properties();
        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
        p.setProperty("source", source);
        p.store(fos,
                "The path of the source corpus is used to decide if the index has to be rebuild - if the path stays the same the existing index is found if any is provided otherwise the index is (re)build");
        fos.close();
    }
}
