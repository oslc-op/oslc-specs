package net.open_services.scheck.shapechecker;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import net.open_services.scheck.annotations.IssueSeverity;


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
    private IssueSeverity	threshold;


    /**
     * Construct a new ResultModelPrinter to print a given set of results to a given stream.
     * @param resultModel the ResultModel to print
     * @param printStream the output stream
     * @param printCrossCheck true iff the cross-check results are to be shown
     * @param threshold issues lower than this severity will not be printed
     */
    public ResultModelPrinter(ResultModel resultModel, PrintStream printStream, boolean printCrossCheck, IssueSeverity threshold)
    {
        this.printStream     = printStream;
        this.resultModel     = resultModel;
        this.printCrossCheck = printCrossCheck;
        this.vocabulary      = resultModel.getResultVocabModel();
        this.threshold       = threshold;
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
            Resource res = r.getProperty(RDF.type).getResource();
            String type;
            if (res.equals(Terms.OntologyResult))
            {
                type = "ontology";
            }
            else if (res.equals(Terms.TermResult))
            {
                type = "vocabulary term";
            }
            else if (res.equals(Terms.ShapeResult))
            {
                type = "resource shape";
            }
            else if (res.equals(Terms.PropertyResult))
            {
                type = "property definition";
            }
            else
            {
                type = "???";
            }
            prefix = String.format("%sWhile examining %s %s:%n", pad(level), type, name);
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
            // or if there are issues found in that section and those issues are at or above the threshold

        	if (level == 0
        		 || (threshold == IssueSeverity.Info && issueCount > 0)
        		 || (threshold == IssueSeverity.Warning && (warnCount > 0 || errorCount > 0))
        		 || (threshold == IssueSeverity.Error && errorCount > 0))
        	{
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
    }


    private void printIssue(int level,Resource issueRes)
    {
        Resource type = issueRes.getPropertyResourceValue(RDF.type);
        IssueSeverity severity = lookupSeverity(type);

        if (severity.compareTo(threshold) >= 0)
    	{
            Resource subjectRes = issueRes.getPropertyResourceValue(Terms.subject);
            Statement badValueSt = issueRes.getProperty(Terms.value);
            String prefix = pad(level);

            printStream.printf("%s%s on %s: %s",
	            prefix,
	            severity,
	            subjectRes.getURI(),
	            unescape(vocabulary.getProperty(type, RDFS.comment).getString()));


	        if (badValueSt != null)
	        {
	            RDFNode badValue = badValueSt.getObject();
	            String badValStr = badValue.isResource() ? badValue.asResource().getURI()
	                : badValue.asLiteral().getLexicalForm();

	            printStream.printf(" (bad value %s)", badValStr);
	        }
	        printStream.println();
    	}
    }


    private void printCrossCheckResults(Resource summary)
    {
        if (printCrossCheck)
        {
            // Process the cross-check results
            printResList(summary,Terms.undefinedTerm);
            printResList(summary,Terms.unusedVocabulary);
            printResList(summary,Terms.unusedTerm);
        }
    }


    private void printResList(Resource summary, Property property)
    {
        StmtIterator sti = summary.listProperties(property);
        if (sti.hasNext())
        {
            IssueSeverity severity = lookupSeverity(property);

            if (severity.compareTo(threshold) >= 0)
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

                printStream.printf("%n%s: %s %s%n",
	                severity,
	                size==1 ? "This "+singular+" was" : "These "+size+" "+plural+" were",
	                message);
	            resURIs.stream().forEachOrdered(s -> printStream.printf("   %s%n",s));
           	}
        }
    }


    @javax.annotation.CheckReturnValue
    private IssueSeverity lookupSeverity(Resource res)
    {
        return IssueSeverity.findSeverity(vocabulary
            .getProperty(res,Terms.severity)
            .getResource()
            .getURI()
            .replaceFirst(".*#",""));
    }


    @javax.annotation.CheckReturnValue
    private String getVocabProp(Property term, Property predicate)
    {
        return vocabulary.getProperty(term, predicate).getString();
    }


    @javax.annotation.CheckReturnValue
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


    @javax.annotation.CheckReturnValue
    private static String pad(int level)
    {
        return level == 0 ? "" : String.format("%"+level*3+"s"," ");
    }


    @javax.annotation.CheckReturnValue
	private static String unescape(String s)
	{
		StringBuilder result = new StringBuilder(s.length());
		int i = 0;
		int n = s.length();
		while (i < n)
		{
			char charAt = s.charAt(i);
			if (charAt != '&')
			{
				result.append(charAt);
				i++;
			}
			else
			{
				if (s.startsWith("&amp;", i))
				{
					result.append('&');
					i += 5;
				}
				else if (s.startsWith("&apos;", i))
				{
					result.append('\'');
					i += 6;
				}
				else if (s.startsWith("&quot;", i))
				{
					result.append('"');
					i += 6;
				}
				else if (s.startsWith("&lt;", i))
				{
					result.append('<');
					i += 4;
				}
				else if (s.startsWith("&gt;", i))
				{
					result.append('>');
					i += 4;
				}
				else
				{
					i++;
				}
			}
		}
		return result.toString();
	}
}
