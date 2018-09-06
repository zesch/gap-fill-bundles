package de.unidue.ltl.preprocess;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;


public class PoSTagPlainText
{
    public static void main(String [] args) throws Exception {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                TextReader.class, TextReader.PARAM_LANGUAGE, "en", 
                TextReader.PARAM_SOURCE_LOCATION,
                "/Users/toobee/Desktop/BundlesNeueDaten/plaintext/B*");
        
        AnalysisEngineDescription tokenizer = AnalysisEngineFactory.createEngineDescription(BreakIteratorSegmenter.class); 
        
        AnalysisEngineDescription tagger = AnalysisEngineFactory.createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_VARIANT, "maxent");

        AnalysisEngineDescription writer = AnalysisEngineFactory.createEngineDescription(
                TokenTagWriter.class, TokenTagWriter.PARAM_TARGET_LOCATION, "/Users/toobee/Desktop/BundlesNeueDaten/B_pos.txt");
        
        SimplePipeline.runPipeline(reader, tokenizer, tagger, writer);
    }
}
