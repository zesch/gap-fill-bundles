package de.unidue.ltl.gapfill.util;

import java.util.ArrayList;
import java.util.List;

public class SubstituteVectorUtil {
	
	
	public static List<SubstituteVector> getBundle(List<SubstituteVector> substituteVectors, int size){
		if(substituteVectors.size() == 0)
			return null;
		
		SubstituteVector best = substituteVectors.get(0);
		double maxD = - Double.MAX_VALUE;
		
		for(SubstituteVector substituteVector : substituteVectors){
			double D = getD(substituteVector);
			if (D > maxD) {
				best = substituteVector;
				maxD = D;
			}
		}
		
		substituteVectors.remove(best);
		
		return getGreedyBundle(best, substituteVectors,size);
	}
	
	public static List<SubstituteVector> getBundle(SubstituteVector bundleVector, List<SubstituteVector> targetVectors, int size){
		return getGreedyBundle(bundleVector, targetVectors,size);
	}
	

	
	public static List<SubstituteVector> getGreedyBundle (SubstituteVector bundleVector, List<SubstituteVector> targetVectors,int size){
		if(size > targetVectors.size() + 1){
			System.err.println("Greater bundle requested than possible - bundle size will be max. size");
			size = targetVectors.size();
		}
		List<SubstituteVector> resultList = new ArrayList<>();
		resultList.add(bundleVector);
		int currentSize = 1;
		
		SubstituteVector combinedVector = bundleVector;
		
		while(currentSize < size){

			SubstituteVector[] subsArr = new SubstituteVector[targetVectors.size()];
			subsArr = targetVectors.toArray(subsArr);
			SubstituteVector bestSub = getBestSubstituteVector(combinedVector, subsArr);
			resultList.add(bestSub);
			targetVectors.remove(bestSub);
			
			combinedVector = combineVectors(combinedVector, bestSub);
			currentSize++;
		}
		
		
		return resultList;
	}

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
	
	public static SubstituteVector getBestSubstituteVector(SubstituteVector bundleVector, SubstituteVector ... targetVectors) {
		if (targetVectors.length == 0) {
			return null;
		}
		
		SubstituteVector result = targetVectors[0];
		double maxD = - Double.MAX_VALUE;	// disambiguation measure to maximize
		
		for (SubstituteVector targetVector : targetVectors) {
			SubstituteVector combinedVector = SubstituteVectorUtil.getCombinedVector(bundleVector, targetVector);
			double D = getD(combinedVector);
			if (D > maxD) {
				result = targetVector;
				maxD = D;
			}
		}
		
		return result;
	}
	
	/**
	 * @param sv
	 * @return The disambiguation measure D given a SubstituteVector
	 */
	public static double getD(SubstituteVector sv) {		
		double targetWeight = sv.getSubstituteWeight(sv.getToken());
		
		// vector was reduced to only target
		if (sv.getSubstitutes().size() == 1) {
			return targetWeight;
		}
		
		double maxAlternativeWeight = - Double.MAX_VALUE;
		for (String substitute : sv.getSubstitutes()) {
			if (!substitute.equals(sv.getToken())) {	// don't compare with target itself
				double substituteWeight = sv.getSubstituteWeight(substitute);
				if (substituteWeight > maxAlternativeWeight) {
					maxAlternativeWeight = substituteWeight;
				}				
			}
		}
		
		return targetWeight - maxAlternativeWeight;
	}
}