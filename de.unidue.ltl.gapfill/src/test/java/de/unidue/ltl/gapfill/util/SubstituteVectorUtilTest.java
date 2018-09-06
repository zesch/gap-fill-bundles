package de.unidue.ltl.gapfill.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubstituteVectorUtilTest {

	private static final SubstituteVector sv1 = new SubstituteVector(
			"money",
			"bug",   -1.3,
			"money", -1.3,
			"green", -1.5
	);
	
	private static final SubstituteVector sv1_b = new SubstituteVector(
			"money",
			"bug",   -3.3,
			"money", -1.3,
			"green", -3.5
	);
	
	private static final SubstituteVector sv2 = new SubstituteVector(
			"money",
			"cash",   -1.9,
			"money",  -2.1,
			"salary", -3.8
	);
	
	private static final SubstituteVector sv3 = new SubstituteVector(
			"money",
			"money",   -0.2,
			"cash",    -1.1,
			"payment", -2.3
	);

	private static final SubstituteVector sv4 = new SubstituteVector(
			"money",
			"geld",   -1.1,
			"zaster", -1.3,
			"money",  -3.2
	);
	
	
	@Test
	public void testVectorCombination() {		
		SubstituteVector result = BundleCompiler.getCombinedVector(sv1, sv2);
		assertEquals(1, result.getSubstitutes().size());
		assertEquals(-3.40000, result.getSubstituteWeight("money"), 0.00001);
		assertEquals(0.0, result.getSubstituteWeight("test"), 0.00001);
		
		SubstituteVector result2 = BundleCompiler.combineMultipleVectors(sv1, sv2);
		assertEquals(1, result2.getSubstitutes().size());
		assertEquals(-3.40000, result2.getSubstituteWeight("money"), 0.00001);
		assertEquals(0.0, result2.getSubstituteWeight("test"), 0.00001);
		
		SubstituteVector result3 = BundleCompiler.combineMultipleVectors(sv1, sv2, sv3, sv4);
		assertEquals(1, result3.getSubstitutes().size());
		assertEquals(-7.60000, result3.getSubstituteWeight("money"), 0.00001);
	}
	
	@Test
	public void testDisambiguationMeasure() {
		assertEquals(0.0, BundleCompiler.getDisambiguity(sv1), 0.01);
		assertEquals(-0.2, BundleCompiler.getDisambiguity(sv2), 0.01);
		assertEquals(0.9, BundleCompiler.getDisambiguity(sv3), 0.01);
		assertEquals(-2.1, BundleCompiler.getDisambiguity(sv4), 0.01);
	}
	
	@Test
	public void testBestVector() {
		SubstituteVector result = BundleCompiler.getBestSubstituteVector(sv1, sv2, sv3, sv4, sv1_b);
		assertEquals(sv1_b, result);
	}
}
