package de.unidue.ltl.gapfill.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class FastSubsConnectorTest {

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
				assertEquals("It", sub.getSubstitutes().get(0).getSubstitute());
				assertEquals(-11.2010, sub.getSubstitutes().get(0).getWeight(), 0.0001);
				
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