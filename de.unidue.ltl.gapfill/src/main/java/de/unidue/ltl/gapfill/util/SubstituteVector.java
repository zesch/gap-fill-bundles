package de.unidue.ltl.gapfill.util;

import java.util.ArrayList;
import java.util.List;

public class SubstituteVector {

	private String token;
	private List<SubstituteEntry> substitutes;
	
	public SubstituteVector() {
		super();
		this.token = "";
		this.substitutes = new ArrayList<>();
	}

	public void addEntry(String substitute, Double weight) {
		substitutes.add(new SubstituteEntry(substitute, weight));
	}
	
	public List<SubstituteEntry> getSubstitutes() {
		return substitutes;
	}

	public void setSubstitutes(List<SubstituteEntry> substitutes) {
		this.substitutes = substitutes;
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
			sb.append(substitutes.get(i).getSubstitute());
			sb.append(" - ");
			sb.append(substitutes.get(i).getWeight());
			sb.append("\n");
		}
		sb.append(" ...");
		
		return sb.toString();
	}
}