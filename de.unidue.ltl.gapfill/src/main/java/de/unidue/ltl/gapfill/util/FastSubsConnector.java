package de.unidue.ltl.gapfill.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

/**
 * Wrapper for the FASTSUBS library.
 * FASTSUBS is a program that finds the most likely substitutes for words using the language model.
 * 
 *  @see <a href="https://github.com/ai-ku/fastsubs">https://github.com/ai-ku/fastsubs</a> 
 */
public class FastSubsConnector implements SubstituteBuilder {
	
	public final static String EXIT_TOKEN = "EXIT_TOKEN";

    private RuntimeProvider runtimeProvider = null;

    private Process process;
    
    private BufferedWriter writer;
	private BufferedReader reader;
	
    private int nrOfSubs;
    private Path languageModelPath;
    
    private Path inputFile;
    private Path outputFile;
    
	public FastSubsConnector() {
		super();
	}
	
	@Override
	public void buildSubstitutes()
		throws IOException, InterruptedException
	{
		// TODO reading input file and directly processing it, does not work
		// TODO parameter for restricting output to top n does not work
		ProcessBuilder pb = new ProcessBuilder(getExecutablePath(),getLanguageModelPath(), "<", inputFile.toAbsolutePath().toString());
		pb.redirectError(Redirect.INHERIT);
		process = pb.start();

	
	    	
		writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
		reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
		
		writeInput(FileUtils.readFileToString(inputFile.toFile()) + " \n");

	    BufferedWriter outputWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE);


        for (String line : captureProcessOutput(EXIT_TOKEN)) {
    		int count = 0;
    		StringBuilder sb = new StringBuilder();
			for (String item : line.split("\t")) {
				if (count <= nrOfSubs) {
					sb.append(item);
					sb.append("\t");
					count++;
				}
			}
        	outputWriter.write(sb.toString());
        	outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
        
        
	}

	@Override
	public void initialize(Path inputFile, Path outputFile, Path languageModelPath, int nrOfSubs) 
	{
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.languageModelPath = languageModelPath;
		this.nrOfSubs = nrOfSubs;
		
		try{
			ProcessBuilder pb = new ProcessBuilder(getExecutablePath(), getLanguageModelPath());
			pb.redirectError(Redirect.INHERIT);
			process = pb.start();
		}catch (IOException e){
			e.printStackTrace();
		}

	}

	public List<SubstituteVector> getSubstitutes(String input) 
			throws IOException
	{
		writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
		reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));

		// write additional line that is used as an indicator when to stop capturing input
		writeInput(input + " \n");

		List<SubstituteVector> listOfSubs = new ArrayList<>();
		
		for (String line : captureProcessOutput("</s>")) {
			listOfSubs.add(fastsubs2vector(line, nrOfSubs));
		}
		return listOfSubs;
	}
	
    private String getExecutablePath()
    		throws IOException
    {
        if (runtimeProvider == null) {            
            runtimeProvider = new RuntimeProvider("classpath:/de/unidue/ltl/fastsubs/");
        }

        String executablePath = runtimeProvider.getFile("fastsubs").getAbsolutePath();

        return executablePath;
    }
    
    private String getLanguageModelPath(){
    	return languageModelPath.toAbsolutePath().toString();
    }
    
    private List<String> captureProcessOutput(String breakCondition) 
    		throws IOException
    {
    	List<String> results = new ArrayList<>();

    	String line = "";
		while ((line = this.reader.readLine()) != null) {
            if (line.startsWith(breakCondition)) {
            	break;
            }
            results.add(line);
        }

        return results;
    }

	private void writeInput(String input) 
			throws IOException
	{
		this.writer.write(input + " \n");
		this.writer.flush();
	}

	public void destroy() 
			throws IOException
	{
		this.writer.flush();
		this.writer.close();
		
		this.reader.close();		
	}
	
	public static SubstituteVector fastsubs2vector(String line, int maxSubs) {
		int count = 0;
		SubstituteVector sv = new SubstituteVector();
		for (String item : line.split("\t")) {
			if (count < maxSubs) {
				String[] parts = item.split(" ");
				
				if (parts.length != 2) {
					sv.setToken(item);
					continue;
				}

				sv.addEntry(parts[0], Double.parseDouble(parts[1]));
				count++;
			}
		}
		return sv;
	}
}
