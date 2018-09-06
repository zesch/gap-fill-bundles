package de.unidue.ltl.gapfill.io;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class GUMReader 
	extends JCasResourceCollectionReader_ImplBase
{
	
    private static final int TOKEN = 0;
    private static final int POS = 1;
    private static final int LEMMA = 2;

	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String mappingPosLocation;
	
	public static final String PARAM_POS_TAGSET = ComponentParameters.PARAM_POS_TAG_SET;
	@ConfigurationParameter(name = PARAM_POS_TAGSET, mandatory = false)
	protected String posTagset;
	
	public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
	@ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = "UTF-8")
	protected String encoding;
	
	/**
	 * Whether coarse-grained or fine-grained POS tags should be used.
	 */
	public static final String PARAM_USE_COARSE_GRAINED = "useCoarseGrained";
	@ConfigurationParameter(name = PARAM_USE_COARSE_GRAINED, mandatory = true, defaultValue = "false")
	protected boolean useCoarseGrained;
		
	private MappingProvider posMappingProvider;
			
	@Override
	public void initialize(UimaContext context)
	    throws ResourceInitializationException
	{
	    super.initialize(context);
	
	    posMappingProvider = new MappingProvider();
	    posMappingProvider.setDefault(MappingProvider.LOCATION,
	            "classpath:/de/tudarmstadt/ukp/dkpro/"
	                    + "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
	    posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
	    posMappingProvider.setDefault("tagger.tagset", "default");
	    posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPosLocation);
	    posMappingProvider.setOverride(MappingProvider.LANGUAGE, getLanguage());
	    posMappingProvider.setOverride("tagger.tagset", posTagset);
	}
	
	public void getNext(JCas aJCas)
	    throws IOException, CollectionException
	{
        try {
			posMappingProvider.configure(aJCas.getCas());
		} catch (AnalysisEngineProcessException e) {
			throw new CollectionException(e);
		}
	    
        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(res.getInputStream(), encoding));
            convert(aJCas, reader);
        }
        finally {
            closeQuietly(reader);
        }
	}
	
	private void convert(JCas aJCas, BufferedReader aReader) 
		throws IOException
	{
	
        JCasBuilder doc = new JCasBuilder(aJCas);

        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                continue;
            }
            
            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            for (String[] word : words) {
                Token token = doc.add(word[TOKEN], Token.class);
                sentenceEnd = token.getEnd();
                doc.add(" ");
                
                Type posTag = posMappingProvider.getTagType(word[POS]);
    	        POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(),
    	                token.getEnd());
    	        pos.setPosValue(word[POS]);
    	        pos.addToIndexes();
    	        
    	        token.setPos(pos);                
            }

            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.addToIndexes();

            // Once sentence per line.
            doc.add("\n");
        }

        doc.close();
    }
	
	/**
     * Read a single sentence.
     */
    private static List<String[]> readSentence(BufferedReader aReader)
        throws IOException
    {
       
	    List<String[]> words = new ArrayList<>();
//	    boolean inFragment=false;
	    String line;
	    while ((line = aReader.readLine()) != null) {
	    	
	    	//uncomment to filter <s type="frag">
	//    	if(readLine.equals("<s type=\"frag\">")){
	//    		inFragment=true;
	//    	}
	//    	if(inFragment){
	//    		if(readLine.equals("</s>"))inFragment=false;
	//    		continue;
	//    	}
	    	
	        if (line.equals("</s>")) {
	            break; // End of sentence
	        }
	        if (line.startsWith("<")) {
	            continue;  // ignore additional tags
	        }
	        
	        String[] fields = line.split("\t");
            if (fields.length != 3) {
                throw new IOException(
                		line + "\n" + 
                "Invalid file format. Line needs to have 3 tab-separted fields.");
            }
            words.add(fields);
	    }
	    
        if (line == null && words.isEmpty()) {
            return null;
        }
        else {
            return words;
        }
    }
}
