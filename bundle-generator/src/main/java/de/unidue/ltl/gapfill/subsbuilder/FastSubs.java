package de.unidue.ltl.gapfill.subsbuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.pear.util.FileUtil;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.unidue.ltl.gapfill.bundeling.SubstituteVector;

/**
 * Wrapper for the FASTSUBS library. FASTSUBS is a program that finds the most likely substitutes
 * for words using the language model.
 * 
 * @see <a href="https://github.com/ai-ku/fastsubs">https://github.com/ai-ku/fastsubs</a>
 */
public class FastSubs
    implements SubstitutionSource
{

    public static final String DOCS_FILE_NAME = "docs.txt";
    public static final String SUBS_FILE_NAME = "subs.txt";
    public final static String EXIT_TOKEN = "EXIT_TOKEN";

    private RuntimeProvider runtimeProvider = null;

    private int nrOfSubs;
    private Path languageModelPath;

    private Path inputFile;
    private Path outputFile;

    public FastSubs(Path indexPath, Path languageModelPath, int nrOfSubs)
    {
        this.inputFile = indexPath.resolve(DOCS_FILE_NAME);
        this.outputFile = indexPath.resolve(SUBS_FILE_NAME);
        this.languageModelPath = languageModelPath;
        this.nrOfSubs = nrOfSubs;

    }

    @Override
    public void buildSubstitutes() throws IOException, InterruptedException
    {
        File tempFile = FileUtil.createTempFile("rawOut", ".txt");

        List<String> command = Arrays.asList(new String[] { getExecutablePath(), "-n",
                nrOfSubs + "", "-o", tempFile.getAbsolutePath(), "-i",
                inputFile.toAbsolutePath().toString(), getLanguageModelPath() });

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        pb.start().waitFor();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(tempFile), "UTF-8"));

        BufferedWriter outputWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE);

        String l = null;
        while ((l = reader.readLine()) != null) {

            if (l.startsWith(EXIT_TOKEN)) {
                break;
            }

            int count = 0;
            StringBuilder sb = new StringBuilder();
            for (String item : l.split("\t")) {
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
        reader.close();
        FileUtils.deleteQuietly(tempFile);
    }

    private String getExecutablePath() throws IOException
    {
        if (runtimeProvider == null) {
            // hey, if the app crashes here ... did you run the ant script? Only OSX/Linux 64 bit
            // supported.
            runtimeProvider = new RuntimeProvider("classpath:/com/github/horsmann/fastsubs-omp/");
        }

        String executablePath = runtimeProvider.getFile("fastsubs-omp").getAbsolutePath();

        return executablePath;
    }

    private String getLanguageModelPath()
    {
        return languageModelPath.toAbsolutePath().toString();
    }

    public static SubstituteVector fastsubs2vector(String line, int maxSubs)
    {
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
