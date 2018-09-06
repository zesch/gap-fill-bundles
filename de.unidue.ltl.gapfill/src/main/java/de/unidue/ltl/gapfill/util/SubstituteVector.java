package de.unidue.ltl.gapfill.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubstituteVector {

	private String token;
	private Map<String, Double> entries;

	// optional attributes that are used to locate the right sentence and position
	private int sentenceId;
	private int tokenId;
	private String sentence;
	private double disambiguity;

	public SubstituteVector() {
		super();
		this.token = "";
		this.entries = new HashMap<>();
	}

	public SubstituteVector(String token, Object... entries) {
		super();
		this.token = token;
		this.entries = new HashMap<>();

		if (entries.length % 2 != 0) {
			throw new IllegalArgumentException("Values for entries must come in String/Double pairs.");
		}

		for (int i = 0; i < entries.length; i = i + 2) {
			String key = entries[i].toString();
			Double value = Double.parseDouble(entries[i + 1].toString());
			this.entries.put(key, value);
		}
	}

	public void addEntry(String substitute, Double weight) {
		if (entries.containsKey(substitute)) {
			throw new IllegalArgumentException("Trying to add duplicate entry for '" + substitute + "'");
		}
		entries.put(substitute, weight);
	}

	public Set<String> getSubstitutes() {
		return entries.keySet();
	}

	public boolean containsSubstitute(String substitute) {
		return entries.containsKey(substitute);
	}

	public Double getSubstituteWeight(String substitute) {
		if (containsSubstitute(substitute)) {
			return entries.get(substitute);
		}
		return 0.0;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getSentenceId() {
		return sentenceId;
	}

	public void setSentenceId(int sentenceId) {
		this.sentenceId = sentenceId;
	}

	public int getTokenId() {
		return tokenId;
	}

	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public String getSentenceWithGap() {
		String[] parts = sentence.split("\t");
		parts[tokenId] = "_______";
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			result.append(parts[i]);
			if (i + 1 < parts.length) {
				result.append(" ");
			}
		}
		return result.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(token);
		sb.append("\n");
		
		List<String> keys = new ArrayList<>(entries.keySet());
		Collections.sort(keys);

		sb.append( " Subs: (");
		for(int i=0; i < keys.size() && i < 5; i++) {
			sb.append(keys.get(i));
			if (i+1 < keys.size()) {
				sb.append(" ");
			}
		}
		sb.append(")");
		
		sb.append("\n");
		
		return sb.toString();
	}

	public void setDisambiguity(double maxD) {
		this.disambiguity = maxD;
	}

	public double getDisambiguity() {
		return this.disambiguity;
	}
}