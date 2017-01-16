package de.unidue.ltl.gapfill.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SubstituteVectorUtil {

//	public SubstituteVector getBestSubstitute(String targetWord, List<SubstituteVector> currentBundle, List<SubstituteVector> targetSubs) {
//		float topWeight=ZERO_PROBABILITY;
//		String topId="";
//		for(String jcasId: vectors.keySet()){
//			//check whether the vector is already contained
//			if(!bestVectorSet.getIds().contains(jcasId)){
//				VectorSet vectorSet= new VectorSet(bestVectorSet.getVectors(),targetWord);
//				vectorSet.addVector(jcasId,vectors.get(jcasId));
////				System.out.println("current match "+vectorSet.getIds() +" "+vectorSet.calculateMeasure());
//				if(vectorSet.calculateMeasure()>topWeight){
////					System.out.println(vectorSet.calculateMeasure()+" > "+topWeight+ " --> select "+jcasId + " instead "+topId);
//					topWeight=vectorSet.calculateMeasure();
//					topId=jcasId;
//				}
//			}
//		}
//		HashMap<String, TargetWordVector> topVector = new HashMap<String, TargetWordVector>();
//		topVector.put(topId,vectors.get(topId));
//		return topVector;
//	}

	public static SubstituteVector combineVectors(SubstituteVector ... vectors) {
		if (vectors.length == 0) {
			return null;
		}
		
		SubstituteVector result = vectors[0];
		for (int i=1; i<vectors.length; i++) {
			result = getCombinedVector(result, vectors[1]);
		}
		
		return result;
	}
	
	public static SubstituteVector getCombinedVector(SubstituteVector v1, SubstituteVector v2) {
		SubstituteVector resultVector = new SubstituteVector(v1.getToken());
		for (String substitute : v1.getSubstitutes()) {
			if (v2.containsSubstitute(substitute)) {
				resultVector.addEntry(
						substitute, 
						v1.getSubstituteWeight(substitute) + v2.getSubstituteWeight(substitute)
				);
			}
		}
		return resultVector;
	}
}
