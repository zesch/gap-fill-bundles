package de.unidue.ltl.gapfill.util;

import java.util.ArrayList;
import java.util.List;

public class WeightedSubstitutes {

	private String token;
	private List<String> substitutes;
	private List<Double> weights;
	
	public WeightedSubstitutes() {
		super();
		this.token = "";
		this.substitutes = new ArrayList<>();
		this.weights = new ArrayList<>();
	}
	
	public WeightedSubstitutes(List<String> substitutes, List<Double> weights) {
		super();
		this.token = "";
		this.substitutes = substitutes;
		this.weights = weights;
	}

	public void addEntry(String substitute, Double weight) {
		substitutes.add(substitute);
		weights.add(weight);
	}
	
	public List<String> getSubstitutes() {
		return substitutes;
	}
	public void setSubstitutes(List<String> substitutes) {
		this.substitutes = substitutes;
	}
	public List<Double> getWeights() {
		return weights;
	}
	public void setWeights(List<Double> weights) {
		this.weights = weights;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(token);
		sb.append("\n");
		
		int limit = Math.min(5, substitutes.size());
		
		for (int i=0; i<limit; i++) {
			sb.append(" ");
			sb.append(substitutes.get(i));
			sb.append(" - ");
			sb.append(weights.get(i));
			sb.append("\n");
		}
		sb.append(" ...");
		
		return sb.toString();
	}
	
	
}