package de.unidue.ltl.gapfill.io;

import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class GumReaderTest {

    @Test
    public void gumReaderTest()
        throws Exception
    {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                GUMReader.class,
				GUMReader.PARAM_SOURCE_LOCATION, "src/main/resources/corpora/GUM",
                GUMReader.PARAM_PATTERNS, "*.xml",
                GUMReader.PARAM_LANGUAGE, "en"
        );		

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
//        	for (POS pos : JCasUtil.select(jcas, POS.class)) {
//        		System.out.println(pos);
//        	}
        	System.out.println(jcas.getDocumentText());
        	i++;
        }
        assertEquals(54, i);
    }
}
