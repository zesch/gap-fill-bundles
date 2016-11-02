package de.unidue.ltl.gapfill.util;

import org.junit.Test;

public class FastSubsConnectorTest {

	@Test
	public void fastsubsTest() 
		throws Exception
	{
		FastSubsConnector fastsubs = new FastSubsConnector(50);
		fastsubs.initialize();
	}
}
