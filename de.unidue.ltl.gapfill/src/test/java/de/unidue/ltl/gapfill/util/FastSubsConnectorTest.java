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
		List<WeightedSubstitutes> subs = fastsubs.run("This is an example");
		
		assertEquals(4, subs.size());
		
		for (WeightedSubstitutes sub : subs) {
			assertEquals(10, sub.getSubstitutes().size());
			
			if (sub.getToken().equals("This")) {
				assertEquals("It", sub.getSubstitutes().get(0));
				assertEquals(-11.2010, sub.getWeights().get(0), 0.0001);
				
			}
			System.out.println(sub);
		}

		List<WeightedSubstitutes> subs2 = fastsubs.run("This is an example");
		assertEquals(4, subs2.size());
		
		for (WeightedSubstitutes sub : subs2) {
			System.out.println(sub);
		}
	}
}