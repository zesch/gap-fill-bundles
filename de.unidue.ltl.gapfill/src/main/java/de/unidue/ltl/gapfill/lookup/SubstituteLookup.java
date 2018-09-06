package de.unidue.ltl.gapfill.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import de.unidue.ltl.gapfill.subsbuilder.FastSubsConnector;
import de.unidue.ltl.gapfill.util.SubstituteVector;
import de.unidue.ltl.gapfill.util.SubstituteVectorUtil;

public class SubstituteLookup
{

    public static final String SENTENCE_FILE_NAME = "sentences.txt";
    public static final String WORDS_FILE_NAME = "words.txt";
    public static final String SUBS_FILE_NAME = "subs.txt";

    private static final String TAB = "\t";

    private BufferedReader wordsReader;
    private BufferedReader subsReader;

    private Path targetLocation;

    private List<String> sentences;

    private int maxSubs;
    private Map<String, String> subsMap;

    public SubstituteLookup(Path targetLocation, int maxSubs) throws Exception
    {
        this.maxSubs = maxSubs;
        this.targetLocation = targetLocation;
        if (Files.notExists(targetLocation)) {
            Files.createDirectories(targetLocation);
        }
        // this.wordsReader = Files.newBufferedReader(targetLocation.resolve(WORDS_FILE_NAME));
        sentences = FileUtils.readLines(targetLocation.resolve(SENTENCE_FILE_NAME).toFile(), "utf-8");
    }

    private String getSentence(int sentenceId) throws IOException
    {
        return sentences.get(sentenceId);
    }

    public List<SubstituteVector> getBundle(int size, String token, String pos) throws Exception
    {
        List<SubstituteVector> substituteVectors = collectSubstitutions(token, pos);

        List<SubstituteVector> result = SubstituteVectorUtil.getBundle(substituteVectors, size);

        int i = 1;
        for (SubstituteVector sv : result) {
            System.out.println(i + ": " + sv.getSentenceWithGap());
            i++;
        }

        return result;
    }

    public List<SubstituteVector> collectSubstitutions(String token, String pos) throws Exception
    {
        this.wordsReader = Files.newBufferedReader(targetLocation.resolve(WORDS_FILE_NAME));
        List<SubstituteVector> substituteVectors = new ArrayList<>();

        String line;
        while ((line = wordsReader.readLine()) != null) {
            if (line.startsWith(token + "_" + pos + "\t"))
                break;
        }

        if (line == null) {
            return Collections.emptyList();
        }

        String[] split = line.split(TAB);
        for (int i = 1; i < split.length; i++) {
            String occurence = split[i];
            String[] sentenceTokenPair = occurence.split("_");
            int sentenceId = Integer.parseInt(sentenceTokenPair[0]);
            int tokenId = Integer.parseInt(sentenceTokenPair[1]);
            substituteVectors.add(getSubstituteVectorForId(token, sentenceId, tokenId));
        }

        return substituteVectors;
    }

    private SubstituteVector getSubstituteVectorForId(String token, int sentenceId, int tokenId)
        throws IOException
    {

        if (subsMap == null) {
            System.out.println("...building substitution map");
            subsMap = new HashMap<>();
            this.subsReader = Files.newBufferedReader(targetLocation.resolve(SUBS_FILE_NAME));

            int currentSentence = 0;
            int currentToken = 0;
            String line = null;
            while ((line = subsReader.readLine()) != null) {

                if (line.startsWith("</s>")) {
                    currentSentence++;
                    currentToken = 0;
                }

                subsMap.put(currentSentence + "_" + currentToken, line);
                currentToken++;
            }

        }

        String line = subsMap.get(sentenceId + "_" + tokenId);
        SubstituteVector sub = FastSubsConnector.fastsubs2vector(line, maxSubs);
        sub.setSentence(getSentence(sentenceId));
        sub.setTokenId(tokenId);
        sub.setToken(token);

        return sub;
    }

}
