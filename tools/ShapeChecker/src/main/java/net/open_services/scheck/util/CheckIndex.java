package net.open_services.scheck.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import net.open_services.scheck.shapechecker.HttpHandler;
import net.open_services.scheck.shapechecker.ShapeCheckException;


/**
 * Verify the jazz.net/ns index page is consistent,
 * matched against a list of known vocabularies.
 *
 * @author Nick Crossley. Released to public domain 2021.
 */
public final class CheckIndex
{
    private Model               vocabModel;
    private HttpHandler         handler = new HttpHandler(0, null);
    private URI                 indexFile = URI.create("https://jazz.net/ns/index.ttl");
    private URI                 listFile;
    private Map<String,Boolean> knownVocabs = new HashMap<>();
    private Set<String>         titleSeen = new HashSet<>();


    private CheckIndex()
    {
        vocabModel = ModelFactory.createDefaultModel();
    }


    /**
     * Verify an index page against a list of known vocabularies.
     * By default, the jazz.net/ns/index.ttl page is checked against standard input.
     * @param args [-i index-page] [-l list-file]
     */
    public static void main(String... args)
    {
        new CheckIndex().run(args);
    }


    private boolean run(String... args)
    {
        return checkUsage(args)
            && readIndexFile()
            && readListFile()
            && checkIndex();
    }


    @javax.annotation.CheckReturnValue
    private boolean checkUsage(String... args)
    {
        int     index  = 0;
        boolean passed = true;

        while (args.length > index && args[index].charAt(0) == '-')
        {
            try
            {
                if (args.length <= index+1)
                {
                    return false;
                }
                else if (args[index].equals("-i"))
                {
                    index++;
                    List<URI> files = GlobExpander.checkFileOrURI(args[index++]);
                    if (files.size() != 1)
                    {
                        return false;
                    }
                    indexFile = files.get(0);
                }
                else if (args[index].equals("-l"))
                {
                    index++;
                    List<URI> files = GlobExpander.checkFileOrURI(args[index++]);
                    if (files.size() != 1)
                    {
                        return false;
                    }
                    listFile = files.get(0);
                }
            }
            catch (URISyntaxException e1)
            {
                System.err.println("Bad URI "+args[index-1]);
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


    @javax.annotation.CheckReturnValue
    private boolean readIndexFile()
    {
        try
        {
            vocabModel.read(indexFile.toString(), "TURTLE");
            return true;
        }
        catch (RiotNotFoundException e)
        {
            System.err.println("Cannot find index file "+indexFile);
            return false;
        }
        catch (RiotException e)
        {
            System.err.println("Cannot parse index file "+indexFile);
            e.printStackTrace();
            return false;
        }

    }


    @javax.annotation.CheckReturnValue
    private boolean readListFile()
    {
        try (BufferedReader lineReader =
                new BufferedReader(new InputStreamReader(
                    listFile == null
                    ? System.in
                    : listFile.toURL().openStream())))
        {
            String line;
            while ((line = lineReader.readLine()) != null)
            {
                knownVocabs.put(line,false);
            }
            return true;
        }
        catch (IOException e)
        {
            System.err.println("Cannot read list file "+listFile);
            e.printStackTrace();
            return false;
        }
    }


    @javax.annotation.CheckReturnValue
    private boolean checkIndex()
    {
        boolean ok = true;

        ResIterator vocabs = vocabModel.listSubjectsWithProperty(RDF.type, OWL.Ontology);

        if (!vocabs.hasNext())
        {
            System.err.println("Cannot find any vocabularies in "+indexFile);
            return false;
        }

        while (vocabs.hasNext())
        {
            Resource ontology = vocabs.next();

            StmtIterator titles = vocabModel.listStatements(ontology, DCTerms.title, (RDFNode)null);
            if (!titles.hasNext())
            {
                System.err.println("Vocabulary at "+ontology.getURI()+" has no title");
            }
            else
            {
                String title = titles.next().getLiteral().getString();
                if (!titleSeen.add(title))
                {
                    System.err.printf("Duplicate title %s on %s%n",title,ontology.getURI());
                }
                if (titles.hasNext())
                {
                    System.err.println("Vocabulary at "+ontology.getURI()+" has multiple titles - copy and paste error?");
                }
            }

            NodeIterator uris = vocabModel.listObjectsOfProperty(ontology, DCTerms.hasFormat);
            while (uris.hasNext())
            {
                RDFNode uri = uris.next();
                String baseName = uri.toString().replaceFirst(".*/","");
                String format = uri.asResource().getProperty(DCTerms.format).getString();
                switch (format)
                {
                case "text/html":
                    if (knownVocabs.containsKey(baseName))
                    {
                        System.out.println("Matched "+baseName);
                        knownVocabs.put(baseName, true);
                    }
                    else
                    {
                        System.err.println("Index contains unknown vocabulary "+baseName);
                    }
                    try
                    {
                        handler.checkValidReference(uri.toString(), false);
                    }
                    catch (ShapeCheckException e)
                    {
                        System.err.println("Bad reference from index to "+uri);
                    }
                    break;
                default:
                    try
                    {
                        String s = uri.toString();
                        if (s.endsWith(".xml"))
                        {
                            System.err.println("Skipping check for archaic index reference to XML "+uri);
                        }
                        else
                        {
                            System.out.println("Checking index reference to "+uri);
                            handler.checkValidReference(uri.toString(), true);
                        }
                    }
                    catch (ShapeCheckException e)
                    {
                        System.err.println("Bad reference from index to "+uri);
                    }
                    break;
                }
            }
        }

        if (knownVocabs.containsValue(false))
        {
            System.err.println("The following known vocabularies are not indexed at "+indexFile);
            knownVocabs.keySet()
                .stream()
                .filter(k -> !knownVocabs.get(k))
                .sorted()
                .forEach(e->System.err.printf("  %s%n", e));
            ok = false;
        }
        return ok;
    }
}
