package de.unidue.ltl.gapfill.util;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FastSubsConnectorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
	
	@Test
	public void batchProcessTest() 
		throws Exception
	{
		String lmPath = "src/test/resources/lm/brown.lm";
		FastSubsConnector fastsubs = new FastSubsConnector(10, lmPath);
		fastsubs.batchProcess(Paths.get("src/test/resources/texts/text.txt"), Paths.get("target/output.txt"));
	}

	@Test
	public void fastsubsTest() 
		throws Exception
	{
		String lmPath = "src/test/resources/lm/brown.lm";
		FastSubsConnector fastsubs = new FastSubsConnector(10, lmPath);
		fastsubs.initialize();
		List<SubstituteVector> subs = fastsubs.getSubstitutes("This is an example");
		
		assertEquals(4, subs.size());
		
		for (SubstituteVector sub : subs) {
			assertEquals(10, sub.getSubstitutes().size());
			
			if (sub.getToken().equals("This")) {
				assertEquals(-11.2010, sub.getSubstituteWeight("It"), 0.0001);
				
			}
			System.out.println(sub);
		}

		List<SubstituteVector> subs2 = fastsubs.getSubstitutes("This is an example");
		assertEquals(4, subs2.size());
		
		for (SubstituteVector sub : subs2) {
			System.out.println(sub);
		}
	}
}