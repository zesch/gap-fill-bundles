package de.unidue.ltl.gapfill.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SubstituteVectorUtilTest {

	private static final SubstituteVector sv1 = new SubstituteVector(
			"money",
			"bug",   -1.3,
			"money", -1.3,
			"green", -1.5
	);
	
	private static final SubstituteVector sv2 = new SubstituteVector(
			"money",
			"cash",   -1.9,
			"money",  -2.1,
			"salary", -3.8
	);
	
	private static final SubstituteVector sv3 = new SubstituteVector(
			"money",
			"cash",    -1.1,
			"money",   -1.2,
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
		SubstituteVector result = SubstituteVectorUtil.getCombinedVector(sv1, sv2);
		assertEquals(1, result.getSubstitutes().size());
		assertEquals(-3.40000, result.getSubstituteWeight("money"), 0.00001);
		assertEquals(0.0, result.getSubstituteWeight("test"), 0.00001);
		
		SubstituteVector result2 = SubstituteVectorUtil.combineVectors(sv1, sv2);
		assertEquals(1, result2.getSubstitutes().size());
		assertEquals(-3.40000, result2.getSubstituteWeight("money"), 0.00001);
		assertEquals(0.0, result2.getSubstituteWeight("test"), 0.00001);
		
		SubstituteVector result3 = SubstituteVectorUtil.combineVectors(sv1, sv2, sv3, sv4);
		assertEquals(1, result3.getSubstitutes().size());
		assertEquals(-7.60000, result3.getSubstituteWeight("money"), 0.00001);
	}
	
//	@Test
//	public void testSumLogProb() throws Exception {
//		VectorSetUtil vectorSetUtil = new VectorSetUtil();
//		float log1=-1;
//		float log2=-1;
//		assertEquals(-0.699,vectorSetUtil.sumLogProb(log1, log2),0.01);
//	}
//	
//	@Test
//	public void getMostProbableTargetWordVectorTest() throws Exception {
//		VectorSetUtil vectorSetUtil = new VectorSetUtil();
//		Map<String, TargetWordVector> jcasToVector = getTestVector();
//		System.out.println(vectorSetUtil.getMostProbableTargetWordVector(jcasToVector,targetWord).getIds());
//		assertEquals("doc3", vectorSetUtil.getMostProbableTargetWordVector(jcasToVector,targetWord).getIds().get(0));
//	}
//
//	@Test
//	public void getOptimalVectorTest() throws Exception {
//		VectorSetUtil vectorSetUtil = new VectorSetUtil();
//		Map<String, TargetWordVector> allVectors = getTestVector();
//		VectorSet bestVectorSet = vectorSetUtil.getMostProbableTargetWordVector(allVectors,targetWord);
//		bestVectorSet.addVectors(vectorSetUtil.getOptimalVector(bestVectorSet, allVectors,targetWord));
//		System.out.println(bestVectorSet.getIds());
//		assertEquals(new ArrayList<String>(Arrays.asList("doc3", "doc1")), bestVectorSet.getIds());
////		bestVectorSet.addVectors(vectorSetUtil.getOptimalVector(bestVectorSet, allVectors));
////		System.out.println(bestVectorSet.getIds());
//	}

}
