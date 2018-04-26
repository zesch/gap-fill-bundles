package de.unidue.ltl.gapfill.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SubstituteVector
{

    private String token;
    private Map<String, Double> entries;

    // optional attributes that are used to locate the right sentence and position
    private int sentenceId;
    private int tokenId;
    private String sentence;

    public SubstituteVector()
    {
        super();
        this.token = "";
        this.entries = new HashMap<>();
    }

    public SubstituteVector(String token, Object... entries)
    {
        super();
        this.token = token;
        this.entries = new HashMap<>();

        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Values for entries must come in String/Double pairs.");
        }

        for (int i = 0; i < entries.length; i = i + 2) {
            String key = entries[i].toString();
            Double value = Double.parseDouble(entries[i + 1].toString());
            this.entries.put(key, value);
        }
    }

    public void addEntry(String substitute, Double weight)
    {
        if (entries.containsKey(substitute)) {
            throw new IllegalArgumentException(
                    "Trying to add duplicate entry for '" + substitute + "'");
        }
        entries.put(substitute, weight);
    }

    public Set<String> getSubstitutes()
    {
        return entries.keySet();
    }

    public boolean containsSubstitute(String substitute)
    {
        return entries.containsKey(substitute);
    }

    public Double getSubstituteWeight(String substitute)
    {
        if (containsSubstitute(substitute)) {
            return entries.get(substitute);
        }
        return 0.0;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public int getSentenceId()
    {
        return sentenceId;
    }

    public void setSentenceId(int sentenceId)
    {
        this.sentenceId = sentenceId;
    }

    public int getTokenId()
    {
        return tokenId;
    }

    public void setTokenId(int tokenId)
    {
        this.tokenId = tokenId;
    }

    public String getSentence()
    {
        return sentence;
    }

    public void setSentence(String sentence)
    {
        this.sentence = sentence;
    }

    public String getSentenceWithGap()
    {
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
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(token);
        sb.append("\n");

        int limit = Math.min(5, entries.size());

        int i = 0;
        for (Map.Entry<String, Double> entry : entries.entrySet()) {
            sb.append(" ");
            sb.append(entry.getKey());
            sb.append(" - ");
            sb.append(entry.getValue());
            sb.append("\n");

            if (i == limit) {
                break;
            }
        }
        sb.append(" ...");

        return sb.toString();
    }
}