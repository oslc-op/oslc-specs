package net.open_services.scheck.shapechecker;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * A set of methods to print a {@link ResultModel}.
 * @author Nick Crossley. Released to public domain 2019.
 */
public class ResultModelPrinter
{
    private PrintStream     printStream;
    private ResultModel     resultModel;
    private Model           vocabulary;
    private boolean         printCrossCheck;


    /**
     * Construct a new ResultModelPrinter to print a given set of results to a given stream.
     * @param resultModel the ResultModel to print
     * @param printStream the output stream
     * @param printCrossCheck true iff the cross-check results are to be shown
     */
    public ResultModelPrinter(ResultModel resultModel, PrintStream printStream, boolean printCrossCheck)
    {
        this.printStream     = printStream;
        this.resultModel     = resultModel;
        this.printCrossCheck = printCrossCheck;
        this.vocabulary      = resultModel.getResultVocabModel();
    }


    /**
     * Print a human-readable form of the result.
     */
    public void print()
    {
        ResultModelProcessor.processResults(
            resultModel,
            r -> printSummary(r),
            r -> printCrossCheckResults(r),
            (l,s,p,r) -> printResults(l,s,p,r),
            (l,s,p,r) -> printIssue(l,r),
            (l,s,p,r) -> {/*no action*/}
            );
    }


    private void printSummary(Resource summary)
    {
        // Results from summary node
        printStream.printf("Results from ShapeChecker run on %s%n",
            ((XSDDateTime)summary.getProperty(DCTerms.created).getLiteral().getValue()).asCalendar().getTime());
        printStream.printf("%nCommand line arguments:%n   %s%n%n",
            wrap(summary.getProperty(DCTerms.description).getString()));
        printIssueCount(-1,"",summary);
    }


    private void printResults(int level, String name, Resource p, Resource r)
    {
        String prefix = "";
        if (r.getProperty(RDF.type).getResource().equals(Terms.VocabResult))
        {
            assert p.getProperty(RDF.type).getResource().equals(Terms.Summary)
                : "Wrong vocab parent type";
            assert level == 0;
            prefix = String.format("%nChecked vocabulary %s from %s",
                name,
                r.getProperty(DCTerms.source).getResource().getURI());
        }
        else if (r.getProperty(RDF.type).getResource().equals(Terms.ShapesResult))
        {
            assert p.getProperty(RDF.type).getResource().equals(Terms.Summary)
            : "Wrong shape parent type";
            assert level == 0;
            prefix = String.format("%nChecked shapes from %s", name);
        }
        else
        {
            assert level > 0;
            prefix = String.format("%sWhile examining %s:%n", pad(level), name);
        }
        printIssueCount(level,prefix,r);
    }


    private void printIssueCount(int level, String prefix, Resource r)
    {
        Statement counts = r.getProperty(Terms.issueCounts);
        if (counts == null)
        {
            printStream.println("Issue count missing!");
            return;
        }

        int infoCount = counts.getProperty(Terms.infoCount).getInt();
        int warnCount = counts.getProperty(Terms.warnCount).getInt();
        int errorCount = counts.getProperty(Terms.errorCount).getInt();
        int issueCount = infoCount + warnCount + errorCount;

        if (level < 0)
        {
            // For the outer summary, we always print the number of issues, even if zero
            printStream.printf("A total of %d issue%s found (%d info, %d warnings, %d errors)%n",
                issueCount, issueCount==1 ? " was" : "s were",
                infoCount, warnCount, errorCount);
        }
        else if (level == 0 || issueCount > 0)
        {
            // We print a section header if its the top level,
            // or if there are issues found in that section
            printStream.print(prefix);
            if (level == 0)
            {
                // We print the number of issues only on the section header,
                // and not for intermediate result levels.
                printStream.printf(", with %d %s (%d info, %d warnings, %d errors)%n",
                    issueCount, issueCount==1 ? "issue" : "issues",
                    infoCount, warnCount, errorCount);
            }
        }
    }


    private void printIssue(int level,Resource issueRes)
    {
        Resource subjectRes = issueRes.getPropertyResourceValue(Terms.subject);
        Resource type = issueRes.getPropertyResourceValue(RDF.type);
        Resource badValue = issueRes.getPropertyResourceValue(Terms.value);
        String severity = lookupSeverity(type);
        String prefix = pad(level);

        if (badValue == null)
        {
            printStream.printf("%s%s on %s: %s%n",
                prefix,
                severity,
                subjectRes.getURI(),
                vocabulary.getProperty(type, RDFS.comment).getString());
        }
        else
        {
            String badValStr = badValue.isResource() ? badValue.getURI()
                : badValue.asLiteral().getLexicalForm();

            printStream.printf("%s%s on %s: %s (bad value %s)%n",
                prefix,
                severity,
                subjectRes.getURI(),
                badValStr,
                vocabulary.getProperty(type, RDFS.comment).getString());
        }
    }


    private void printCrossCheckResults(Resource summary)
    {
        if (printCrossCheck)
        {
            // Process the cross-check results
            printResList(summary,Terms.undefinedClass);
            printResList(summary,Terms.undefinedProp);
            printResList(summary,Terms.unusedVocabulary);
            printResList(summary,Terms.unusedTerm);
        }
    }


    private void printResList(Resource summary, Property property)
    {
        StmtIterator sti = summary.listProperties(property);
        if (sti.hasNext())
        {
            Iterable<Statement> it = (Iterable<Statement>)() -> sti;
            List<String> resURIs = StreamSupport.stream(it.spliterator(),false)
                .map(st->st.getResource().getURI())
                .sorted()
                .collect(Collectors.toList());
            int size = resURIs.size();
            String message = getVocabProp(property, RDFS.comment);
            String singular = getVocabProp(property, Terms.singular);
            String plural = getVocabProp(property, Terms.plural);
            String severity = lookupSeverity(property);

            printStream.printf("%n%s: %s %s%n",
                severity,
                size==1 ? "This "+singular+" was" : "These "+size+" "+plural+" were",
                message);
            resURIs.stream().forEachOrdered(s -> printStream.printf("   %s%n",s));
        }
    }


    private String lookupSeverity(Resource res)
    {
        return vocabulary
            .getProperty(res,Terms.severity)
            .getResource()
            .getURI()
            .replaceFirst(".*#","");
    }


    private String getVocabProp(Property term, Property predicate)
    {
        return vocabulary.getProperty(term, predicate).getString();
    }


    private static String wrap(String msg)
    {
        StringBuilder sb = new StringBuilder(msg);
        int lineLength = 120;

        int i = 0;
        while (i + lineLength < sb.length() && (i = sb.lastIndexOf(" ", i + lineLength)) != -1)
        {
            sb.replace(i, i + 1, "\n   ");
        }

        return sb.toString();
    }


    private static String pad(int level)
    {
        return level == 0 ? "" : String.format("%"+level*3+"s"," ");
    }
}
