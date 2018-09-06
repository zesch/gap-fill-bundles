package de.unidue.ltl.gapfill.bundeling;

public class BundleVector {

	double disambiguity;
	SubstituteVector vector;

	public BundleVector(SubstituteVector vector, double disambiguity) {
		this.vector = vector;
		this.disambiguity = disambiguity;
		
	}

	public String getSentenceWithGap() {
		return vector.getSentenceWithGap();
	}

	public double getDisambiguity() {
		return disambiguity;
	}
}
