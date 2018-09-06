package de.unidue.ltl.gapfill.indexer;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.AnalysisEngineFactory;

import de.unidue.ltl.gapfill.subsbuilder.FastSubsConnector;

public class IndexBuilder
{

    private Path indexPath;
    private CollectionReaderDescription reader;
    private int LIMIT;
    private int MAX_SENT_LEN;
    private String model;

    public IndexBuilder path(Path indexPath)
    {
        this.indexPath = indexPath;
        return this;
    }

    public IndexBuilder reader(CollectionReaderDescription reader)
    {
        this.reader = reader;
        return this;
    }

    public IndexBuilder limit(int LIMIT)
    {
        this.LIMIT = LIMIT;
        return this;
    }

    public IndexBuilder sentLen(int MAX_SENT_LEN)
    {
        this.MAX_SENT_LEN = MAX_SENT_LEN;
        return this;
    }

    public IndexBuilder model(String model)
    {
        this.model = model;
        return this;
    }

    public void build() throws Exception
    {
        Path lmPath = Paths.get(model);

        CorpusIndexer indexer = new CorpusIndexer(indexPath, reader, AnalysisEngineFactory.createEngineDescription(NoOpAnnotator.class),
                LIMIT, MAX_SENT_LEN);
        indexer.index();

        System.out.println(" --- retrieving substitutes");
        FastSubsConnector subsBuilder = new FastSubsConnector(indexPath, lmPath, LIMIT);
        subsBuilder.buildSubstitutes();
    }

}
