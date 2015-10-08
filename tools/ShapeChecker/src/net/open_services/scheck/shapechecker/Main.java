package net.open_services.scheck.shapechecker;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;


/**
 * Main entrypoint to OSLC Shape & Vocabulary checker.
 * @author Nick Crossley. Released to public domain.
 */
public class Main
{
    private List<URI> vocabularies = new ArrayList<>();
    private List<URI> shapes       = new ArrayList<>();

    /**
     * Main entrypoint to OSLC Shape & Vocabulary checker
     * @param args command line arguments specify vocabularies and shapes to be checked
     * <ul>
     * <li>Each -v argument introduces a vocabulary, by local path or by URI</li>
     * <li>Each -s argument introduces a shape, by local path or by URI</li>
     * <li>Each -q argument names an issue to be ignored
     * </ul>
     * The arguments may be repeated to check multiple vocabulary and shape documents.
     */
    public static void main(String[] args)
    {
        new Main().run(args);
    }


    private void run(String[] args)
    {
        ResultModel resultModel = new ResultModel();
        HttpHandler httpHandler = new HttpHandler();

        if (!checkUsage(args,resultModel))
        {
            System.err.println("Usage: "+this.getClass().getName()+" -q suppressedIssue ... -s shapeFile|shapeURI ... -v vocabFile|vocabURI ...");
        }

        int errors = 0;

        for (URI vocab : vocabularies)
        {
            try
            {
                errors += new VocabularyCheck(vocab, httpHandler, resultModel).checkVocabularies();
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
                errors += new ShapesDocCheck(shape, httpHandler, resultModel).checkShapes();
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

        if (vocabularies.size() != 0 && shapes.size() != 0)
        {
            CrossCheck crossCheck = new CrossCheck(resultModel);
            crossCheck.buildMaps();
            errors += crossCheck.check();
        }

        resultModel.getSummary().addLiteral(ResultModel.issueCount, errors);
//        Models.write(resultModel.getModel(), System.out);
        resultModel.print(System.out);
    }


    private boolean checkUsage(String[] args, ResultModel resultModel)
    {
        int     index  = 0;
        boolean passed = true;

        while (args.length > index+1 && args[index].charAt(0) == '-')
        {
            try
            {
                if (args[index].equals("-v") ||  args[index].equals("-vocab"))
                {
                    index++;
                    vocabularies.add(checkFileOrURI(args[index++]));
                }
                else if (args[index].equals("-s") ||  args[index].equals("-shape"))
                {
                    index++;
                    shapes.add(checkFileOrURI(args[index++]));
                }
                else if (args[index].equals("-q") ||  args[index].equals("-quiet"))
                {
                    index++;
                    resultModel.suppressIssue(args[index++]);
                }
                else
                {
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
            return false;
        }
        return passed;
    }


    /**
     * Make a URI for an argument that is either a URI string or a file path.
     * @param argVal an argument that is either a URI string or a file path
     * @return a URI that is either one formed from the provided string, or a file: URI for a local path
     * @throws URISyntaxException if the URI is not valid
     */
    public URI checkFileOrURI(String argVal) throws URISyntaxException
    {
        if (argVal.startsWith("http://") || argVal.startsWith("https://"))
        {
            return new URI(argVal);
        }
        else
        {
            return new File(argVal).toURI();
        }
    }
}
