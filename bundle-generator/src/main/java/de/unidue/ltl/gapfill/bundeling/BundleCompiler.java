package de.unidue.ltl.gapfill.bundeling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundleCompiler
{

    public static List<BundleVector> getBundle(List<SubstituteVector> substituteVectors,
            int size)
    {
        if (substituteVectors.size() == 0) {
            return Collections.emptyList();
        }

        SubstituteVector best = substituteVectors.get(0);
        double maxDisambiguity = -Double.MAX_VALUE;

        for (SubstituteVector substituteVector : substituteVectors) {
            double disambiguity = getDisambiguity(substituteVector);
            if (disambiguity > maxDisambiguity) {
                best = substituteVector;
                maxDisambiguity = disambiguity;
            }
        }

        substituteVectors.remove(best);

        List<BundleVector> greedyBundle = getGreedyBundle(best, substituteVectors, size);

        if (greedyBundle.size() < size) {
            return Collections.emptyList();
        }

        return greedyBundle;
    }

    private static List<BundleVector> getGreedyBundle(SubstituteVector bundleVector,
            List<SubstituteVector> targetVectors, int size)
    {
        if (size > targetVectors.size() + 1) {
            System.err.println(
                    "Not enough sentences found, cannot return bundle of size ["+size+"]");
            return Collections.emptyList();
        }
        List<BundleVector> resultList = new ArrayList<>();
        resultList.add(new BundleVector(bundleVector, 0.0));
        int currentSize = 1;

        SubstituteVector combinedVector = bundleVector;

        while (currentSize < size) {

            SubstituteVector[] subsArr = new SubstituteVector[targetVectors.size()];
            subsArr = targetVectors.toArray(subsArr);
            BundleVector bestSub = getBestSubstituteVector(combinedVector, subsArr);
            resultList.add(bestSub);
            targetVectors.remove(bestSub.vector);

            combinedVector = combineMultipleVectors(combinedVector, bestSub.vector);
            currentSize++;
        }

        return resultList;
    }

    static SubstituteVector combineMultipleVectors(SubstituteVector... vectors)
    {
        if (vectors.length == 0) {
            return null;
        }

        SubstituteVector result = vectors[0];
        for (int i = 1; i < vectors.length; i++) {
            result = getCombinedVector(result, vectors[1]);
        }

        return result;
    }

    static SubstituteVector getCombinedVector(SubstituteVector v1, SubstituteVector v2)
    {
        SubstituteVector resultVector = new SubstituteVector(v1.getToken());
        for (String substitute : v1.getSubstitutes()) {
            if (v2.containsSubstitute(substitute)) {
                resultVector.addEntry(substitute,
                        v1.getSubstituteWeight(substitute) + v2.getSubstituteWeight(substitute));
            }
        }
        return resultVector;
    }

    static BundleVector getBestSubstituteVector(SubstituteVector bundleVector,
            SubstituteVector... targetVectors)
    {
        if (targetVectors.length == 0) {
            return null;
        }

        SubstituteVector result = targetVectors[0];
        double maxD = -Double.MAX_VALUE; // disambiguation measure to maximize

        for (SubstituteVector targetVector : targetVectors) {
            SubstituteVector combinedVector = BundleCompiler.getCombinedVector(bundleVector,
                    targetVector);
            double D = getDisambiguity(combinedVector);
            if (D > maxD) {
                result = targetVector;
                maxD = D;
            }
        }
        
        return new BundleVector(result, maxD);
    }

    /**
     * @param sv
     * @return The disambiguation measure D given a SubstituteVector
     */
    static double getDisambiguity(SubstituteVector sv)
    {
        double targetWeight = sv.getSubstituteWeight(sv.getToken());

        // vector was reduced to only target
        if (sv.getSubstitutes().size() == 1) {
            return targetWeight;
        }

        double maxAlternativeWeight = -Double.MAX_VALUE;
        for (String substitute : sv.getSubstitutes()) {
            if (!substitute.equals(sv.getToken())) { // don't compare with target itself
                double substituteWeight = sv.getSubstituteWeight(substitute);
                if (substituteWeight > maxAlternativeWeight) {
                    maxAlternativeWeight = substituteWeight;
                }
            }
        }

        return targetWeight - maxAlternativeWeight;
    }
}