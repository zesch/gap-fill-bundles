package de.unidue.ltl.gapfill.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.unidue.ltl.fastSubs.container.TargetWordVector;
import de.unidue.ltl.fastSubs.container.VectorSet;
import de.unidue.ltl.fastSubs.main.MeasureForBinCases;
import de.unidue.ltl.gapfill.GapFillConstants;

public class VectorSetUtil 
	implements GapFillConstants
{

	/**
	 * searches through the whole set for the vector that increases the MEASURE the most
	 * @param bestVectorSet current bestVectorSet
	 * @param vectors all other vectors
	 * @return targetWord jcasToVector
	 */
	public Map<String, TargetWordVector> getOptimalVector(VectorSet bestVectorSet, Map<String, TargetWordVector> vectors, String targetWord) {
		float topWeight=ZERO_PROBABILITY;
		String topId="";
		for(String jcasId: vectors.keySet()){
			//check whether the vector is already contained
			if(!bestVectorSet.getIds().contains(jcasId)){
				VectorSet vectorSet= new VectorSet(bestVectorSet.getVectors(),targetWord);
				vectorSet.addVector(jcasId,vectors.get(jcasId));
//				System.out.println("current match "+vectorSet.getIds() +" "+vectorSet.calculateMeasure());
				if(vectorSet.calculateMeasure()>topWeight){
//					System.out.println(vectorSet.calculateMeasure()+" > "+topWeight+ " --> select "+jcasId + " instead "+topId);
					topWeight=vectorSet.calculateMeasure();
					topId=jcasId;
				}
			}
		}
		HashMap<String, TargetWordVector> topVector = new HashMap<String, TargetWordVector>();
		topVector.put(topId,vectors.get(topId));
		return topVector;
	}
	
	
	/**
	 * searches through the whole set for the vector that minimizes the difference of the targets weight and the weight of the (differing )top substitute
	 * @param bestVectorSet current bestVectorSet
	 * @param vectors all other vectors
	 * @return targetWord jcasToVector
	 */
	public Map<String, TargetWordVector> getOptimalVectorByMinimizingDifferenceToTopCandidate(VectorSet bestVectorSet, Map<String, TargetWordVector> vectors, String targetWord) {
		float topWeight=ZERO_PROBABILITY;
		String topId="";
		for(String jcasId: vectors.keySet()){
			//check whether the vector is already contained
			if(!bestVectorSet.getIds().contains(jcasId)){
				VectorSet vectorSet= new VectorSet(bestVectorSet.getVectors(),targetWord);
				vectorSet.addVector(jcasId,vectors.get(jcasId));
//				System.out.println("current match "+vectorSet.getIds() +" "+vectorSet.calculateMeasure());
				
				if(vectorSet.calculateDifferenceOfTargetToTopProbableSub()>topWeight){
//					System.out.println(vectorSet.calculateMeasure()+" > "+topWeight+ " --> select "+jcasId + " instead "+topId);
					topWeight=vectorSet.calculateDifferenceOfTargetToTopProbableSub();
					topId=jcasId;
				}
			}
		}
		HashMap<String, TargetWordVector> topVector = new HashMap<String, TargetWordVector>();
		topVector.put(topId,vectors.get(topId));
		return topVector;
	}
	
	/**
	 * returns the vector which assigns the highest probability to the target word
	 * @param jcasToVector
	 * @return VectorSet
	 */
	public VectorSet getMostProbableTargetWordVector(Map<String, TargetWordVector> jcasToVector,String targetWord) {
		float topWeight=ZERO_PROBABILITY;
		String topId="";
		for(String jcasId: jcasToVector.keySet()){
			float probability =jcasToVector.get(jcasId).getWeightofTarget();
//			System.out.println(jcasId+ " "+ probability);
			if(probability>topWeight){
				topWeight=probability;
				topId=jcasId;
			}
		}
		HashMap<String, TargetWordVector> topWeightedJCAS = new HashMap<String, TargetWordVector>();
		topWeightedJCAS.put(topId,jcasToVector.get(topId));
		System.out.println("top probable vector "+topId+ " weight "+topWeight);
		return new VectorSet(topWeightedJCAS,targetWord);
	}
	
	/**
	 * returns the vector which maximizes our fitness function
	 * @param jcasToVector
	 * @return VectorSet
	 */
	public VectorSet getMostFittedVector(Map<String, TargetWordVector> jcasToVector,String targetWord) {
		float topWeight=ZERO_PROBABILITY;
		String topId="";
		for(String jcasId: jcasToVector.keySet()){
			
			Map<String, TargetWordVector> vectors = new HashMap<>();
			vectors.put(targetWord, jcasToVector.get(jcasId));
			VectorSet vectorSet= new VectorSet(vectors,targetWord);
			float probability =vectorSet.calculateMeasure();
//			System.out.println(jcasId+ " "+ probability);
			if(probability>topWeight){
				topWeight=probability;
				topId=jcasId;
			}
		}
		HashMap<String, TargetWordVector> topWeightedJCAS = new HashMap<String, TargetWordVector>();
		topWeightedJCAS.put(topId,jcasToVector.get(topId));
//		System.out.println("top probable vector "+topId+ " weight "+topWeight);
		return new VectorSet(topWeightedJCAS,targetWord);
	}


	public static float sumLogProb(Float log1, float log2) {
		float sum=(float) (Math.pow(10, log1))+(float)(Math.pow(10, log1));
		return (float) Math.log10(sum);
	}


	/**
	 * new criterium for selecting the seed sentence
	 * we now select if the delta to the top probable non target substitute is max
	 * @param vectors
	 * @param targetWord
	 * @return
	 */
	public VectorSet getVectorWithBiggestD(Map<String, TargetWordVector> vectors, String targetWord) {
		float d=ZERO_PROBABILITY;
		String topId="";
		for(String jcasId: vectors.keySet()){
			TargetWordVector vec= vectors.get(jcasId);
			float weightOfTarget= vec.getWeightofTarget();
			float weightOfTopNonTarget=vec.getWeightOfTopNonTarget();
			float currentD=weightOfTarget-weightOfTopNonTarget;
			if(currentD>d){
				d=currentD;
				topId=jcasId;
			}
		}
		HashMap<String, TargetWordVector> topWeightedJCAS = new HashMap<String, TargetWordVector>();
		topWeightedJCAS.put(topId,vectors.get(topId));
		System.out.println("top probable vector "+topId+ " weight "+d);
		return new VectorSet(topWeightedJCAS,targetWord);
	}

/**
 * selects a Vector from vector set at random
 * @param vectors
 * @param targetWord
 * @return
 */
	public VectorSet getRandomVector(Map<String, TargetWordVector> vectors, String targetWord) {
		float d=ZERO_PROBABILITY;
		List<String> allIds= new ArrayList<String>(vectors.keySet());
		Random random= new Random();
		int index = random.nextInt(allIds.size());
	    String topId = allIds.get(index);
		
		HashMap<String, TargetWordVector> topWeightedJCAS = new HashMap<String, TargetWordVector>();
		topWeightedJCAS.put(topId,vectors.get(topId));
		System.out.println("random vector "+topId+ " weight "+d);
		return new VectorSet(topWeightedJCAS,targetWord);
	}


public VectorSet getPreselectedRandomVector(Map<String, TargetWordVector> vectors, String targetWord) {
	
	String id=MeasureForBinCases.targetToIds.get(targetWord).iterator().next();
	HashMap<String, TargetWordVector> topWeightedJCAS = new HashMap<String, TargetWordVector>();
	topWeightedJCAS.put(id,vectors.get(id));
	
	return new VectorSet(topWeightedJCAS, targetWord);
}
	
	
}
