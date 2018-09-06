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
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.unidue.ltl.gapfill.bundeling.BundleVector;
import de.unidue.ltl.gapfill.indexer.BundleIndexBuilder;
import de.unidue.ltl.gapfill.io.LineTokenTagReader;
import de.unidue.ltl.gapfill.lookup.SubstituteLookup;

public class ExecuteBundleGeneration
{
    int LIMIT = 250;
    int MAX_SENT_LEN=100;
    String MODEL="";

    FrequencyDistribution<String> nouns = new FrequencyDistribution<>();
    FrequencyDistribution<String> verbs = new FrequencyDistribution<>();
    FrequencyDistribution<String> adjectives = new FrequencyDistribution<>();

    String SOURCE;
    String LANGUAGE;
    String indexLocation;
    String outputFolder;
    String nounPre;
    String verbPre;
    String adjPre;

    public static void main(String[] args) throws Exception
    {
    	
    	ExecuteBundleGeneration bundles = new ExecuteBundleGeneration();
    	bundles.generate(new File(args[0]));
    }
    
    public void generate(File config) throws Exception {
        Properties p = new Properties();
        FileInputStream f = new FileInputStream(config);
        p.load(f);
        f.close();

        MODEL = p.getProperty("model");
        MAX_SENT_LEN = Integer.parseInt(p.getProperty("sentLen"));
        LIMIT = Integer.parseInt(p.getProperty("numSubs"));
        SOURCE = p.getProperty("folder");
        LANGUAGE = p.getProperty("lang");
        
        indexLocation = p.getProperty("index");
        outputFolder = p.getProperty("output");
        nounPre = p.getProperty("nounPrefix");
        verbPre = p.getProperty("verbPrefix");
        adjPre = p.getProperty("adjPrefix");
        
        buildFrequencyDis(nouns, nounPre);
        buildFrequencyDis(verbs, verbPre);
        buildFrequencyDis(adjectives, adjPre);

        Set<FrequencyDistribution<String>> fds = new HashSet<>();
        fds.add(nouns);
        fds.add(verbs);
        fds.add(adjectives);

        cleanOutputFolder(outputFolder);
        
        buildIndex(indexLocation, SOURCE + "_" + MAX_SENT_LEN + "_" + LIMIT
                + "_" + LANGUAGE + "_" + nounPre + verbPre + adjPre);
        Path indexPath = Paths.get(indexLocation);

        for (FrequencyDistribution<String> d : fds) {

            List<String> mostFrequentSamples = d.getMostFrequentSamples(50);

            for (String e : mostFrequentSamples) {
                int lastIndexOf = e.lastIndexOf("_");
                String word = e.substring(0, lastIndexOf);
                String pos = e.substring(lastIndexOf + 1);

                System.out.println(" --- creating bundles for [" + word + "/" + pos + "]");
                SubstituteLookup sl = new SubstituteLookup(indexPath, LIMIT);
                List<BundleVector> bundle = sl.getBundle(4, word, pos);

                if (bundle.isEmpty()) {
                    System.out.println("Could not create bundle for " + word + "/" + pos);
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                for (BundleVector s : bundle) {
                    sb.append(s.getSentenceWithGap() + "( " + s.getDisambiguity() + " )\n");
                    sb.append("\n");
                }

                FileUtils.writeStringToFile(
                        new File(outputFolder, pos + "_" + word + "_" + "bundle.txt"),
                        sb.toString(), "utf-8");
                
                System.out.println();
            }
        }
    }

    private void buildIndex(String indexLocation, String idKey) throws Exception
    {

        if (rebuildIndex(indexLocation, idKey)) {
            System.out.println("Building index information");

            BundleIndexBuilder builder = new BundleIndexBuilder();
            builder.path(Paths.get(indexLocation))
                   .reader(getReader())
                   .limit(LIMIT)
                   .sentLen(MAX_SENT_LEN)
                   .model(MODEL)
                   .index();
        }
        else {
            System.out.println(
                    "Use existing index from previous runs at [" + indexLocation + "]");
        }        
    }

    private static void cleanOutputFolder(String outputFolder)
    {
        File folder = new File(outputFolder);
        FileUtils.deleteQuietly(folder);
        boolean mkdirs = folder.mkdirs();
        if (!mkdirs) {
            throw new IllegalStateException("Could not create output folder");
        }
    }

    private CollectionReaderDescription getReader() throws ResourceInitializationException
    {
        return CollectionReaderFactory.createReaderDescription(LineTokenTagReader.class,
                LineTokenTagReader.PARAM_SOURCE_LOCATION, SOURCE,
                LineTokenTagReader.PARAM_PATTERNS, "*.txt", LineTokenTagReader.PARAM_LANGUAGE,
                LANGUAGE);
    }

    private void buildFrequencyDis(FrequencyDistribution<String> fd, String prefix)
        throws ResourceInitializationException
    {

        for (JCas jcas : new JCasIterable(getReader())) {
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

        System.out.println(
                "POS-Prefix: " + prefix + " found [" + fd.getB() + "] distinct word/pos pairs");
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
