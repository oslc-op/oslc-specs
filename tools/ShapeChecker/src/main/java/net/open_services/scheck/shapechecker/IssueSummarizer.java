package net.open_services.scheck.shapechecker;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import net.open_services.scheck.annotations.IssueSeverity;


/**
 * A set of methods to count the issues in a {@link ResultModel}.
 * @author Nick Crossley. Released to public domain 2019.
 */
public class IssueSummarizer
{
    private ResultModel         resultModel;
    private Model               vocabulary;
    private Deque<IssueCounter> issueCounters = new ArrayDeque<>();


    /**
     * Construct a new IssueSummarizer to count the issues in the given ResultModel.
     * @param resultModel the ResultModel for which to count issues
     */
    public IssueSummarizer(ResultModel resultModel)
    {
        this.resultModel    = resultModel;
        this.vocabulary     = resultModel.getResultVocabModel();
    }


    /**
     * Count the issues found in the results,
     * and add those counts to new sc:issueCount nodes in the result model.
     * @return the number of errors (ignoring info and warning issues)
     */
    public int countIssues()
    {
        ResultModelProcessor.processResults(
            resultModel,
            r -> startCounts(),
            r -> addCounts(r),
            (l,s,p,r) -> startCounts(),
            (l,s,p,r) -> incrementCounts(r),
            (l,s,p,r) -> addCounts(r));

        Statement counts = resultModel.getSummary().getProperty(Terms.issueCounts);
        if (counts == null)
        {
            System.err.println("Issue count missing!");
            return 1;
        }
        return counts.getProperty(Terms.errorCount).getInt();
    }


    private void startCounts()
    {
        issueCounters.push(new IssueCounter());
    }


    private void addCounts(Resource r)
    {
        IssueCounter previous = issueCounters.pop();
        addIssueCounts(previous,r);

        IssueCounter current = issueCounters.peek();
        if (current != null)
        {
            current.infoCount += previous.infoCount;
            current.warnCount += previous.warnCount;
            current.errorCount += previous.errorCount;
        }
    }


    private void addIssueCounts(IssueCounter counter,Resource parent)
    {
        Resource counts = resultModel.getModel().createResource()
                .addLiteral(Terms.infoCount, counter.infoCount)
                .addLiteral(Terms.warnCount, counter.warnCount)
                .addLiteral(Terms.errorCount, counter.errorCount);
        parent.addProperty(Terms.issueCounts, counts);
    }


    private void incrementCounts(Resource issueRes)
    {
        Resource issueType = issueRes.getPropertyResourceValue(RDF.type);
        IssueSeverity severity = IssueSeverity.findSeverity(
            vocabulary
                .getProperty(issueType,Terms.severity)
                .getResource()
                .getURI()
                .replaceFirst(".*#",""));

        IssueCounter current = issueCounters.peek();
        switch (severity)
        {
        case Info: current.infoCount++;
            break;
        case Warning: current.warnCount++;
            break;
        case Error: current.errorCount++;
            break;
        default:
            break;
        }
    }

    //CSOFF: Visibility
    static final class IssueCounter
    {
        int infoCount  = 0;
        int warnCount  = 0;
        int errorCount = 0;
    }
}
