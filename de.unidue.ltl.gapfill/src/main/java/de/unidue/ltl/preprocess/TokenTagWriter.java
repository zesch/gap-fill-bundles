package de.unidue.ltl.preprocess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenTagWriter
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private String targetLocation;

    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;
    
    public static final String PARAM_WRITE_COARSE = "PARAM_WRITE_COARSE";
    @ConfigurationParameter(name = PARAM_WRITE_COARSE, mandatory = true, defaultValue = "false")
    private boolean coarse;

    private static StringBuilder sb = new StringBuilder();

    public static final String PARAM_MISSING_POS = "PARAM_MISSING_POS";
    @ConfigurationParameter(name = PARAM_MISSING_POS, mandatory = false, defaultValue = "XYZ")
    private String missingPosDummy;

    private BufferedWriter buffWrite = null;

    Set<String> tags = new HashSet<>();
    
    int tokens=0;
    
    @Override
    public void initialize(final UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        try {
            File file = new File(targetLocation);
            file.getParentFile().mkdirs();
            buffWrite = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), encoding));
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {

        for (Sentence s : JCasUtil.select(aJCas, Sentence.class)) {
            sb = new StringBuilder();
            for (Token token : JCasUtil.selectCovered(aJCas, Token.class, s.getBegin(),
                    s.getEnd())) {
                String posValue = missingPosDummy;
                POS pos = token.getPos();
                if (pos != null) {
                    if(!coarse){
                        posValue = pos.getPosValue();    
                    }else{
                        posValue = pos.getClass().getSimpleName();
                    }
                }
                sb.append(token.getCoveredText() + " " + posValue + "\n");
                tags.add(posValue);
                tokens++;
            }
            sb.append("\n");
            write(buffWrite, sb);
        }

    }

    private void write(BufferedWriter aBf, StringBuilder aSb)
        throws AnalysisEngineProcessException
    {
        try {
            buffWrite.write(sb.toString());
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        for(String t : tags){
            System.out.println(t);
        }
        System.out.println("Number of tokens written: " + tokens);
        
        if (buffWrite != null) {
            try {
                buffWrite.close();
                buffWrite = null;
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

}
