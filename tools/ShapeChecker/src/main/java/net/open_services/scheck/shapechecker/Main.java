package net.open_services.scheck.shapechecker;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;

import net.open_services.scheck.util.GlobExpander;



/**
 * Main entry point to OSLC Shape and Vocabulary checker.
 * @author Nick Crossley. Released to public domain 2015.
 */
public class Main
{
    private List<URI> vocabularies = new ArrayList<>();
    private List<URI> shapes       = new ArrayList<>();
    private boolean   debug        = false;
    private boolean   verbose      = false;
    private boolean   crossCheck   = true;

    /**
     * Main entry point to OSLC Shape and Vocabulary checker.
     * @param args command line arguments specify vocabularies and shapes to be checked
     * <ul>
     * <li>Each -v/--vocab argument introduces a vocabulary, by local path or by URI</li>
     * <li>Each -s/--shape argument introduces a shape, by local path or by URI</li>
     * <li>Each -q/--quiet argument names an issue to be ignored</li>
     * <li>Each -x/--exclude argument specifies a regular expression for URIs not to be read</li>
     * <li>-N/--nocrosscheck turns off the cross-checking of vocabularies and shapes</li>
     * <li>-V/--verbose turns out more progress information</li>
     * <li>-D/--debug turns on debugging output</li>
     * </ul>
     * The arguments may be repeated to check multiple vocabulary and shape documents.
     * The vocabulary and shape arguments can use globs (*.ttl, etc.)
     */
    public static void main(String... args)
    {
        new Main().run(args);
    }


    private void run(String... args)
    {
        ResultModel resultModel = new ResultModel(args);
        HttpHandler httpHandler = new HttpHandler();

        if (!checkUsage(args,resultModel,httpHandler))
        {
            System.err.println("Usage: "+this.getClass().getName()
                + " [-s|--shape shapeFile|shapeURI ...]"
                + " [-v|--vocab vocabFile|vocabURI ...]"
                + " [-q|--quiet suppressedIssue ...]"
                + " [-x|--exclude excludeURIPattern ...]"
                + " [-N|--nocrosscheck]"
                + " [-V|--verbose] [-D]"
                );
            System.exit(1);
        }

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

        for (URI vocab : vocabularies)
        {
            try
            {
                if (verbose)
                {
                    System.err.println("Parsing "+vocab);
                }
                new VocabularyCheck(vocab, httpHandler, resultModel).checkVocabularies();
            }
            catch (RiotNotFoundException e)
            {
                System.err.println("Cannot find vocabulary document "+vocab);
            }
            catch (RiotException e)
            {
                System.err.println("Cannot parse vocabulary document "+vocab);
                e.printStackTrace();
            }
        }
        for (URI shape : shapes)
        {
            try
            {
                if (verbose)
                {
                    System.err.println("Parsing "+shape);
                }
                new ShapesDocCheck(shape, httpHandler, resultModel).checkShapes();
            }
            catch (RiotNotFoundException e)
            {
                System.err.println("Cannot find shape document "+shape);
            }
            catch (RiotException e)
            {
                System.err.println("Cannot parse shape document "+shape);
                e.printStackTrace();
            }
        }

        if (!vocabularies.isEmpty() && (verbose || crossCheck))
        {
            CrossCheck crossChecker = new CrossCheck(resultModel);
            crossChecker.buildMaps(verbose);
            if (crossCheck && !shapes.isEmpty())
            {
                crossChecker.check();
            }
        }

        int errors = resultModel.summarizeIssues();
        if (debug)
        {
            Models.write(resultModel.getModel(), System.out);
        }
        new ResultModelPrinter(resultModel,System.out,crossCheck).print();

        if (errors > 0)
        {
            System.exit(1);
        }
    }


    @javax.annotation.CheckReturnValue
    private boolean checkUsage(String[] args, ResultModel resultModel, HttpHandler httpHandler)
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
                    debug = true;
                    System.err.println("Arguments: "+String.join(" ",args));
                }
                if (args[index].equals("-V") || args[index].equals("--verbose"))
                {
                    index++;
                    verbose = true;
                    httpHandler.setVerbose(verbose);
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
                else if (args[index].equals("-v") || args[index].equals("--vocab"))
                {
                    index++;
                    vocabularies.addAll(checkFileOrURI(args[index++]));
                }
                else if (args[index].equals("-s") || args[index].equals("--shape"))
                {
                    index++;
                    shapes.addAll(checkFileOrURI(args[index++]));
                }
                else if (args[index].equals("-q") || args[index].equals("--quiet"))
                {
                    index++;
                    resultModel.suppressIssue(args[index++]);
                }
                else if (args[index].equals("-x") || args[index].equals("--exclude"))
                {
                    index++;
                    httpHandler.excludeURIPattern(args[index++]);
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


    /**
     * Make a list of URIs for an argument that is either a single URI string,
     * or a file path that contains shell-style globs to be expanded.
     * @param argVal an argument that is either a URI string or a file path with globs
     * @return a list of URIs that are formed from the provided string
     * @throws URISyntaxException if the URI is not valid
     */
    @javax.annotation.CheckReturnValue
    public List<URI> checkFileOrURI(String argVal) throws URISyntaxException
    {
        if (argVal.startsWith("http://") || argVal.startsWith("https://"))
        {
            return Collections.singletonList(new URI(argVal));
        }
        else
        {
           List<URI> uris = new ArrayList<>();
           for (String path : GlobExpander.expand(argVal))
           {
               uris.add(new File(path).toURI());
           }
           if (uris.isEmpty())
           {
               System.err.println("Warning: nothing matches "+argVal);
           }
           return uris;
        }
    }
}
