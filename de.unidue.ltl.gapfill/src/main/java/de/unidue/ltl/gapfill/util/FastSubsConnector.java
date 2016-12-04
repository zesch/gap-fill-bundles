package de.unidue.ltl.gapfill.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public class FastSubsConnector {

    private RuntimeProvider runtimeProvider = null;

    private Process process;
    
    private BufferedWriter writer;
	private BufferedReader reader;
	
    private int nrOfSubs;
    private String languageModelPath;
    
	public FastSubsConnector(int nrOfSubs, String languageModelPath) {
		super();
		this.nrOfSubs = nrOfSubs;
		this.languageModelPath = languageModelPath;
	}

	public void initialize() 
			throws IOException
	{
		List<String> evalCommand = new ArrayList<String>();
		evalCommand.add(getExecutablePath());
//		evalCommand.add("-n");
//		evalCommand.add(String.valueOf(nrOfSubs));
		evalCommand.add(languageModelPath);

		ProcessBuilder pb = new ProcessBuilder().command(evalCommand);
		pb.redirectError(Redirect.INHERIT);
		pb.command(evalCommand);
		process = pb.start();
	}

	public List<SubstituteVector> getSubstitutes(String input) 
			throws IOException
	{
		writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
		reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));

		// write additional line that is used as an indicator when to stop capturing input
		writeInput(input + " \n");

		List<SubstituteVector> listOfSubs = new ArrayList<>();
		
		for (String line : captureProcessOutput()) {

			int count = 0;
			SubstituteVector subs = new SubstituteVector();
			for (String item : line.split("\t")) {
				if (count < nrOfSubs) {
					String[] parts = item.split(" ");
					
					if (parts.length != 2) {
						subs.setToken(item);
						continue;
					}

					subs.addEntry(parts[0], Double.parseDouble(parts[1]));
					count++;
				}

			}
			listOfSubs.add(subs);
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
    
    private List<String> captureProcessOutput() 
    		throws IOException
    {
    	List<String> results = new ArrayList<>();

    	String line = "";
		while ((line = this.reader.readLine()) != null) {
            if (line.startsWith("</s>")) {
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
}
