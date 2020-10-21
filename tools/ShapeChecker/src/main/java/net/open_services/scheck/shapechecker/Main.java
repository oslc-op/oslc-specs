package net.open_services.scheck.shapechecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;

import net.open_services.scheck.annotations.IssueSeverity;
import net.open_services.scheck.util.GlobExpander;



/**
 * Main entry point to OSLC Shape and Vocabulary checker.
 * @author Nick Crossley. Released to public domain 2015.
 */
public class Main
{
    private List<URI>     vocabularies     = new ArrayList<>();
    private List<URI>     shapes           = new ArrayList<>();
    private Set<String>   shapesWanted     = new HashSet<>();
    private Set<String>   shapesSeen       = new HashSet<>();
    private Set<Pattern>  skipURIPatterns  = new HashSet<>();
    private int           debug            = 0;
    private boolean       verbose          = false;
    private boolean       crossCheck       = true;
    private boolean       checkConstraints = false;
    private IssueSeverity threshold        = IssueSeverity.Info;
    private CrossCheck    crossChecker;


    /**
     * Main entry point to OSLC Shape and Vocabulary checker.
     * @param args command line arguments specify vocabularies and shapes to be checked
     * <ul>
     * <li>Each -v/--vocab argument introduces a vocabulary, by local path or by URI</li>
     * <li>Each -s/--shape argument introduces a shape, by local path or by URI</li>
     * <li>Each -q/--quiet argument names an issue to be ignored</li>
     * <li>Each -x/--exclude argument specifies a regular expression for URIs not to be read</li>
     * <li>-t/--threshold this defines a threshold for reporting issues;
     *    issues with a lower severity will not be reported.
     *    The default is {@code info}, so by default all issues are reported.</li>
     * <li>-C/--constraints require extra metadata on vocabularies and shapes for OSLC Specifications</li>
     * <li>-N/--nocrosscheck turns off the cross-checking of vocabularies and shapes</li>
     * <li>-V/--verbose turns out more progress information</li>
     * <li>-D/--debug turns on debugging output; multiple -D flags increase the debug level</li>
     * </ul>
     * The arguments may be repeated to check multiple vocabulary and shape documents.
     * The vocabulary and shape arguments can use shell-style globs (*.ttl, etc.),
     * but just to be different, the exclude option uses full regular expressions, not globs.
     */
    public static void main(String... args)
    {
        new Main().run(args);
    }


    private void run(String... args)
    {
        ResultModel resultModel   = new ResultModel(args);
        boolean        failedToParse = false;

        if (!checkUsage(args,resultModel))
        {
            System.err.println("Usage: "+this.getClass().getName()
                + " [-v|--vocab vocabFileGlob|vocabURI ...]"
                + " [-s|--shape shapeFileGlob|shapeURI ...]"
                + " [-q|--quiet suppressedIssue ...]"
                + " [-x|--exclude excludeURIPattern ...]"
                + " [-t|--threshold severityThreshold]"
                + " [-C|--constraints]"
                + " [-N|--nocrosscheck]"
                + " [-V|--verbose]"
                + " [-D|--debug]"
                );
            System.exit(1);
        }

        HttpHandler httpHandler = new HttpHandler(debug, skipURIPatterns);

        // TODO: there's a fundamental problem here in the way the tables are built for the cross-check.
        // Instantiation of VocabularyCheck loads a vocabulary document into memory (good), and
        // runs the checks on that vocabulary, which in turn loads many of the resources referenced from
        // vocabulary (bad). It is bad because those references might be part of a second or subsequent
        // vocabulary document to be checked, but which has not yet been loaded or processed. So
        // references between vocabularies, or references from shapes to items loaded from resources that
        // were referenced from vocabularies can end up looking at out-of-date vocabularies loaded from
        // their real locations on the internet, and not from the source being checked.
        // TODO: to fix this, separate vocabulary loading from checking, and pre-load all the vocabs first.
        // Meanwhile, work around this by setting @base in the source to make the URIs for terms reflect the
        // intended target, and use the -x command line option to exclude that (presumably older) target.

        // Check each specified RDF Vocabulary
        for (URI vocab : vocabularies)
        {
            try
            {
                if (verbose)
                {
                    System.out.println("Parsing "+vocab);
                }
                new VocabularyCheck(vocab, httpHandler, resultModel, checkConstraints).checkVocabularies();
            }
            catch (RiotNotFoundException e)
            {
                System.err.println("Cannot find vocabulary document "+vocab);
                failedToParse = true;
            }
            catch (RiotException e)
            {
                System.err.println("Cannot parse vocabulary document "+vocab);
                e.printStackTrace();
                failedToParse = true;
            }
        }

        // Check each specified set of OSLC Resource Shapes
        for (URI shape : shapes)
        {
            try
            {
                if (verbose)
                {
                    System.out.println("Parsing "+shape);
                }
                new ShapesDocCheck(shape, shapesSeen, shapesWanted, httpHandler, resultModel, checkConstraints).checkShapes();
            }
            catch (RiotNotFoundException e)
            {
                System.err.println("Cannot find shape document "+shape);
                failedToParse = true;
            }
            catch (RiotException e)
            {
                System.err.println("Cannot parse shape document "+shape);
                e.printStackTrace();
                failedToParse = true;
            }
        }

        // Check that terms are defined and used
        if (verbose || crossCheck)
        {
            crossChecker = new CrossCheck(resultModel);
            crossChecker.buildMaps();
            shapesWanted.removeAll(shapesSeen);

            if (crossCheck && !shapes.isEmpty())
            {
                crossChecker.check(shapesWanted);
            }
        }

        if (debug > 2)
        {
            System.err.println("\nResult model before summarizing:");
            Models.write(resultModel.getModel(), System.err);
        }

        // Scan the result model, adding issue counts
        int errors = resultModel.summarizeIssues(debug);

        if (debug > 2)
        {
            System.err.println("\nResult model after summarizing:");
            Models.write(resultModel.getModel(), System.err);
        }

        // Print ShapeChecker results
        // Add blank line before results if we previously printed some progress messages
        if (verbose)
        {
            System.out.println();
        }
        new ResultModelPrinter(resultModel,System.out,crossCheck,threshold).print();

        // Print list of vocabulary terms if requested
        if (verbose && crossChecker != null)
        {
            crossChecker.printVocabTerms();
        }

        if (failedToParse || errors > 0)
        {
            System.exit(1);
        }
    }


    @javax.annotation.CheckReturnValue
    private boolean checkUsage(String[] args, ResultModel resultModel)
    {
        int     index  = 0;
        boolean passed = true;

        while (args.length > index && args[index].charAt(0) == '-')
        {
            try
            {
                if (args[index].equals("-D") || args[index].equals("--debug"))
                {
                    index++;
                    debug++;
                    if (debug==1)
                    {
                        System.err.println("Arguments: "+String.join(" ",args));
                    }
                }
                else if (args[index].equals("-C") || args[index].equals("--constraints"))
                {
                    index++;
                    checkConstraints = true;
                }
                else if (args[index].equals("-V") || args[index].equals("--verbose"))
                {
                    index++;
                    verbose = true;
                }
                else if (args[index].equals("-N") || args[index].equals("--nocrossheck"))
                {
                    index++;
                    crossCheck = false;
                }
                else if (args.length <= index+1)
                {
                    return false;
                }
                else if (args[index].equals("-t") || args[index].equals("--threshold"))
                {
                    index++;
                    String arg = args[index++];
                    try
                    {
                        threshold = IssueSeverity.findSeverity(arg);
                    }
                    catch (IllegalArgumentException e)
                    {
                        System.err.println("Invalid severity threshold "+arg);
                        return false;
                    }
                }
                else if (args[index].equals("-v") || args[index].equals("--vocab"))
                {
                    index++;
                    vocabularies.addAll(GlobExpander.checkFileOrURI(args[index++]));
                }
                else if (args[index].equals("-s") || args[index].equals("--shape"))
                {
                    index++;
                    shapes.addAll(GlobExpander.checkFileOrURI(args[index++]));
                }
                else if (args[index].equals("-q") || args[index].equals("--quiet"))
                {
                    index++;
                    resultModel.suppressIssue(args[index++]);
                }
                else if (args[index].equals("-x") || args[index].equals("--exclude"))
                {
                    index++;
                    skipURIPatterns.add(Pattern.compile(args[index++]));
                }
                else
                {
                    System.err.println("Unexpected option "+args[index]);
                    return false;
                }
            }
            catch (URISyntaxException e1)
            {
                System.err.println("Bad URI "+args[index-1]);
                passed = false;
            }
            catch (NoSuchElementException e2)
            {
                System.err.println("Unknown issue name "+args[index-1]);
                passed = false;
            }
        }

        if (args.length > index)
        {
            System.err.println("Unexpected argument "+args[index]);
            return false;
        }
        return passed;
    }
}
