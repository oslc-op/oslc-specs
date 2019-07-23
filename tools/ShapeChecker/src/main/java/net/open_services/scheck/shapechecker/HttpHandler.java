package net.open_services.scheck.shapechecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RiotException;


/**
 * Handle http connections, and verify the reachability of URIs.
 * @author Nick Crossley. Released to public domain 2015.
 */
public class HttpHandler
{
    private Map<URI,Boolean> httpResourceIsRDF = new HashMap<>();
    private Set<String> foundRDFResources = new HashSet<>();
    private Set<Pattern> skipURIPatterns = new HashSet<>();
    private boolean debug = false;


    /**
     * Sets the debug option.
     * @param debug the debug value to set.
     */
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }


    /**
     * Exclude a URI pattern from being fetched and parsed.
     * @param uri the URI pattern to be skipped
     */
    public void excludeURIPattern(String uri)
    {
        skipURIPatterns.add(Pattern.compile(uri));
    }


    /**
     * Check to see if a URI is reachable.
     * @param uri the uri to check
     * @throws ShapeCheckException if there was a problem checking the URI
     */
    public void checkValidReference(String uri) throws ShapeCheckException
    {
        URI httpUri;

        if (foundRDFResources.contains(uri))
        {
            // Resource previously found, or noted as undefined
            return;
        }

        try
        {
            httpUri = removeFragment(uri);
        }
        catch (URISyntaxException e)
        {
            throw new ShapeCheckException(
                Terms.InvalidUri,
                ResourceFactory.createResource(uri),
                ResourceFactory.createResource(e.toString()),
                e);
        }

        try
        {
            fetchHttpResource(httpUri);
            findRDFResource(httpUri,uri);
        }
        catch (RiotException | HttpException e1)
        {
            httpResourceIsRDF.put(httpUri, false);
            throw new ShapeCheckException(
                Terms.InvalidRdfError,
                ResourceFactory.createResource(uri),
                ResourceFactory.createResource(e1.toString()),
                e1);
        }
    }


    /**
     * Remove the fragment portion of a URI.
     * @param uri the uri whose fragment, if any, is to be removed
     * @return the uri minus any fragment part
     * @throws URISyntaxException if the URI is invalid
     */
    @javax.annotation.CheckReturnValue
    public URI removeFragment(String uri) throws URISyntaxException
    {
        URI httpUri = new URI(uri);
        StringBuilder sb = new StringBuilder();
        sb.append(httpUri.getScheme());
        sb.append("://");
        sb.append(httpUri.getHost());
        if (httpUri.getPort() > 0)
        {
            sb.append(':');
            sb.append(httpUri.getPort());
        }
        sb.append(httpUri.getPath());
        return new URI(sb.toString());
    }


    private void fetchHttpResource(URI httpUri) throws HttpException
    {
        if (httpResourceIsRDF.containsKey(httpUri))
        {
            // Resource previously found
        }
        else if (containsMatch(skipURIPatterns,httpUri.toString()))
        {
            // Do not try to read or parse this
            httpResourceIsRDF.put(httpUri, false);
        }
        else
        {
            // Resource is RDF, so read it and save contained subjects
            httpResourceIsRDF.put(httpUri, true);
            if (debug)
            {
                System.err.println("Parsing "+httpUri);
            }
            Model foundModel = ModelFactory.createDefaultModel().read(httpUri.toString());
            ResIterator ri = foundModel.listSubjects();
            while (ri.hasNext())
            {
                // Skip any blank node subjects
                String uri = ri.next().getURI();
                if (uri != null)
                {
                    foundRDFResources.add(uri);
                }
            }
        }
    }


    private static boolean containsMatch(Set<Pattern> patterns, String str)
    {
        for (Pattern p : patterns)
        {
            if (p.matcher(str).matches())
            {
                return true;
            }
        }
        return false;
    }


    @javax.annotation.CheckReturnValue
    private void findRDFResource(URI httpUri, String uri) throws ShapeCheckException
    {
        if (httpUri.toString().equals(uri) || !httpResourceIsRDF.get(httpUri))
        {
            // The HTTP resource itself has already been found
            // We do not check non-RDF hash resources
            return;
        }
        else if (foundRDFResources.contains(uri))
        {
            // RDF Resource has been found
            return;
        }
        else
        {
            // Not found - but add it so we report an error only once
            foundRDFResources.add(uri);
            throw new ShapeCheckException(
                Terms.UndefinedTerm,
                ResourceFactory.createResource(uri),
                ResourceFactory.createResource(httpUri.toString()));
        }
    }
}
