package de.unidue.ltl.gapfill.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public class FastSubsConnector {

	private BufferedWriter writer;
	private BufferedReader reader;
    private RuntimeProvider runtimeProvider = null;

    private int nrOfSubs;
    
    

	public FastSubsConnector(int nrOfSubs) {
		super();
		this.nrOfSubs = nrOfSubs;
	}

	public void initialize() 
			throws IOException
	{
		List<String> evalCommand = new ArrayList<String>();
		evalCommand.add(getExecutablePath());
		evalCommand.add("src/main/test/lm/test.lm");
		evalCommand.add(String.valueOf(nrOfSubs));

		ProcessBuilder pb = new ProcessBuilder().command(evalCommand);
		pb.redirectError(Redirect.INHERIT);
		pb.command(evalCommand);
		Process process = pb.start();
		writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
		reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
	}

    private String getExecutablePath()
    		throws IOException
    {

        if (runtimeProvider == null) {
            PlatformDetector pd = new PlatformDetector();
            String platform = pd.getPlatformId();
            
            runtimeProvider = new RuntimeProvider("classpath:/de/unidue/ltl/fastsubs/");
        }

        String executablePath = runtimeProvider.getFile("fastsubs").getAbsolutePath();

        return executablePath;
    }

//		@Override
//		public void process(JCas jcas) throws AnalysisEngineProcessException {
//			for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
//				try {
//					List<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);
//					// concatenate token with spaces text instead of using sentence
//					// text
//					// otherwise we can not make sure the same number tokens will be
//					// returned
//					// (fastsubs use simple white space tokenization)
//					List<String> toTextLowercase = this.toTextLowercase(tokens);
//					writeInput(StringUtils.join(toTextLowercase, " "));
//					// System.out.println(StringUtils.join(toTextLowercase, " "));
//
//					if (useWeighting) {
//						// parse output and get a position to subs mapping
//						Map<Integer, TokenWithWeightedSubs> tokenToSubs = readOutput(jcas, tokens.size());
//
//						// reassign fastSub subs to tokens per position
//						int i = 0;
//						for (Token token : JCasUtil.selectCovered(Token.class, sentence)) {
//							FastSubsWeighted annotation = new FastSubsWeighted(jcas);
//							// System.out.println(token.getCoveredText());
//							// System.out.println(tokenToSubs);
//							// System.out.println(token.getCoveredText() + " " +
//							// tokenToSubs.get(i));
//							// System.out.println(tokenToSubs.get(i).getSubs());
//							annotation.setWeightedSubs(tokenToSubs.get(i).getSubs());
//							annotation.setBegin(token.getBegin());
//							annotation.setEnd(token.getEnd());
//							annotation.addToIndexes();
//							i++;
//						}
//					}
//				} catch (IOException e) {
//					System.err.println("SEVERE");
//					throw new AnalysisEngineProcessException(e);
//				}
//			}
//
//		}
//
//		protected List<String> toTextLowercase(List<Token> tokens) {
//			List<String> text = new ArrayList<String>();
//			for (Token t : tokens) {
//				text.add(t.getCoveredText().toLowerCase());
//			}
//			return text;
//		}
//
//		protected void writeInput(String sb) throws IOException {
//
//			this.writer.write(sb.toString() + " " + System.lineSeparator());
//			this.writer.flush();
//		}
//
//		/**
//		 * parse output from the c binary by creatimg a mapping between the position
//		 * of the tokens and the TokenWithSubs container
//		 * 
//		 * @param jcas
//		 * @return
//		 * @throws IOException
//		 */
//		protected Map<Integer, TokenWithWeightedSubs> readOutput(JCas jcas, int noOfTokens) throws IOException {
//			Map<Integer, TokenWithWeightedSubs> result = new HashMap<Integer, TokenWithWeightedSubs>();
//
//			String line = null;
//			String currentKey = "";
//			int currentPoition = 0;
//			int currentSubNumber = 0;
//			Map<String, Float> subBuffer = new LinkedHashMap<String, Float>();
//
//			boolean firstResults = true;
//			int processedSubs = 0;
//			while ((line = this.reader.readLine()) != null) {
//
//				// System.out.println(line);
//				// handle lines with subs
//				if (line.split("\t").length > 1) {
//					// System.out.println("\t"+line+ " "+processedTokens);
//					processedSubs++;
//					currentSubNumber++;
//					subBuffer.put(line.split("\t")[0], Float.parseFloat(line.split("\t")[1]));
//				}
//				// handle lines that specify the keys
//				else {
//					// System.out.println(line);
//					if (!firstResults) {
//						TokenWithWeightedSubs tokenWithSubs = new TokenWithWeightedSubs(currentKey, jcas, currentSubNumber);
//						tokenWithSubs.setArray(subBuffer);
//						result.put(currentPoition, tokenWithSubs);
//						// clear buffer and SubNumber
//						subBuffer.clear();
//						currentSubNumber = 0;
//						currentPoition++;
//					} else {
//						firstResults = false;
//					}
//					currentKey = line.replace(":", "");
//				}
//				// check whether
//				if (processedSubs >= (noOfTokens + 1) * numberOfSubs) {
//					// System.out.println("all tokens substituted ");
//					break;
//				}
//				// if (!this.reader.ready()) {
//				// System.out.println("reader not ready");
//				// break;
//				// }
//			}
//			// add last read
//			TokenWithWeightedSubs tokenWithSubs = new TokenWithWeightedSubs(currentKey, jcas, currentSubNumber);
//			tokenWithSubs.setArray(subBuffer);
//			result.put(currentPoition, tokenWithSubs);
//			return result;
//		}
//	}

}
