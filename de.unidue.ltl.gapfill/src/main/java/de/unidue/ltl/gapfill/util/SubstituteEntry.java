package de.unidue.ltl.gapfill.util;

public class SubstituteEntry {

	private String substitute;
	private Double weight;
		
	public SubstituteEntry(String substitute, Double weight) {
		this.substitute = substitute;
		this.weight = weight;
	}
	
	public String getSubstitute() {
		return substitute;
	}
	
	public void setSubstitute(String substitute) {
		this.substitute = substitute;
	}
	
	public Double getWeight() {
		return weight;
	}
	
	public void setWeight(Double weight) {
		this.weight = weight;
	}
}