package de.unidue.ltl.gapfill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.gapfill.indexer.CorpusIndexer;
import de.unidue.ltl.gapfill.io.LineTokenTagReader;
import de.unidue.ltl.gapfill.lookup.SubstituteLookup;
import de.unidue.ltl.gapfill.subsbuilder.FastSubsConnector;
import de.unidue.ltl.gapfill.util.SubstituteVector;

public class BundleExample
{
    static int LIMIT = 250;

    static FrequencyDistribution<String> nouns = new FrequencyDistribution<>();
    static FrequencyDistribution<String> verbs = new FrequencyDistribution<>();
    static FrequencyDistribution<String> adjectives = new FrequencyDistribution<>();

    public static void main(String[] args) throws Exception
    {
        Properties p = new Properties();
        FileInputStream f = new FileInputStream(args[0]);
        p.load(f);
        f.close();

        //// for (int i = 1; i < args.length; i += 2) {
        // String word = args[i];
        // String pos = args[i + 1];
        //
        // System.out.println(word + " " + pos);

        String sourceFolder = p.getProperty("folder");
        String model = p.getProperty("model");
        String indexLocation = p.getProperty("index");
        String outputFolder = p.getProperty("output");
        String lang = p.getProperty("lang");
        int MAX_SENT_LEN = Integer.parseInt(p.getProperty("sentLen"));
        LIMIT = Integer.parseInt(p.getProperty("numSubs"));

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                LineTokenTagReader.class, LineTokenTagReader.PARAM_SOURCE_LOCATION, sourceFolder,
                LineTokenTagReader.PARAM_PATTERNS, "*.txt", LineTokenTagReader.PARAM_LANGUAGE,
                lang);

        buildFrequencyDis(reader, nouns, "N");
        buildFrequencyDis(reader, verbs, "V");
        buildFrequencyDis(reader, adjectives, "A");

        Set<FrequencyDistribution<String>> fds = new HashSet<>();
        fds.add(nouns);
        fds.add(verbs);
        fds.add(adjectives);

        for (FrequencyDistribution<String> d : fds) {

            List<String> mostFrequentSamples = d.getMostFrequentSamples(250);

            for (String e : mostFrequentSamples) {
                int lastIndexOf = e.lastIndexOf("_");
                String word = e.substring(0, lastIndexOf);
                String pos = e.substring(lastIndexOf + 1);

                Path indexPath = Paths.get(indexLocation);

                if (rebuildIndex(indexLocation,
                        sourceFolder + "_" + MAX_SENT_LEN + "_" + LIMIT + "_" + lang)) {
                    System.out.println("Building index information");

                    AnalysisEngineDescription preprocessing = AnalysisEngineFactory
                            .createEngineDescription(NoOpAnnotator.class);
                    Path lmPath = Paths.get(model);

                    CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, preprocessing,
                            LIMIT, MAX_SENT_LEN);
                    indexer.index();

                    System.out.println(" --- retrieving substitutes");
                    FastSubsConnector subsBuilder = new FastSubsConnector(indexPath, lmPath, LIMIT);
                    subsBuilder.buildSubstitutes();
                }
                else {
                    System.out.println(
                            "Use existing index from previous runs at [" + indexLocation + "]");
                }

                System.out.println(" --- creating bundles for [" + word + "/" + pos + "]");
                SubstituteLookup sl = new SubstituteLookup(indexPath, LIMIT);
                List<SubstituteVector> bundle = sl.getBundle(4, word, pos);

                if (bundle.isEmpty()) {
                    System.out.println("Could not create bundle for " + word + "/" + pos);
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                for (SubstituteVector s : bundle) {
                    sb.append(s.getSentenceWithGap() + "\n");
                    sb.append("\n");
                }

                new File(outputFolder).mkdirs();

                FileUtils.writeStringToFile(
                        new File(outputFolder, pos + "_" + word + "_" + "bundleResult.txt"),
                        sb.toString());
                // }
            }
        }
    }

    private static void buildFrequencyDis(CollectionReaderDescription reader,
            FrequencyDistribution<String> fd, String prefix)
    {

        for (JCas jcas : new JCasIterable(reader)) {
            Collection<Token> tokens = JCasUtil.select(jcas, Token.class);
            for (Token t : tokens) {
                POS pos = t.getPos();
                if (pos == null) {
                    continue;
                }
                if (pos.getPosValue().startsWith(prefix)) {
                    fd.addSample(t.getCoveredText() + "_" + pos.getPosValue(), 1);
                }
            }

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
        file.getParentFile().mkdirs();

        Properties p = new Properties();
        FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
        p.setProperty("source", source);
        p.store(fos,
                "The path of the source corpus is used to decide if the index has to be rebuild - if the path stays the same the existing index is found if any is provided otherwise the index is (re)build");
        fos.close();
    }
}
