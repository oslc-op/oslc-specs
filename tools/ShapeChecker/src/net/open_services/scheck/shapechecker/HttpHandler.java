package net.open_services.scheck.shapechecker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RiotException;


/**
 * Handle http connections, and verify the reachability of URIs.
 * @author Nick Crossley. Released to public domain.
 */
public class HttpHandler
{
    private static final String JENA_RDF_ACCEPT_HEADER = "text/turtle,application/n-triples;q=0.9,application/ld+json;q=0.8,application/rdf+xml;q=0.7,*/*;q=0.5";
    private static final String RDF_TYPES = "turtle|n-triples|json|rdf|xml";

    private Map<URI,Boolean> foundHttpResources = new HashMap<>();
    private Set<String> foundRDFResources = new HashSet<>();
    private Set<String> skipURIs = new HashSet<>();
    private boolean verbose = false;


    /**
     * Sets the verbose option.
     * @param verbose the verbose value to set.
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }


    /**
     * Exclude a URI from being fetched and parsed.
     * @param uri the URI to be skipped
     */
    public void excludeURI(String uri)
    {
        skipURIs.add(uri);
    }


    /**
     * Check to see if a URI is reachable.
     * @param uri the uri to check
     * @throws ShapeCheckException
     */
    public void checkValidReference(String uri) throws ShapeCheckException
    {
        URI httpUri;

        if (foundRDFResources.contains(uri))
        {
            // Resource previously found
            return;
        }

        try
        {
            httpUri = removeFragment(uri);
        }
        catch (URISyntaxException e)
        {
            throw new ShapeCheckException(
                ResultModel.InvalidUri,
                ResourceFactory.createResource(uri),
                null);
        }

        try
        {
            fetchHttpResource(httpUri);
            findRDFResource(httpUri,uri);
        }
        catch (RiotException | IOException e1)
        {
            foundHttpResources.put(httpUri, false);
            throw new ShapeCheckException(
                ResultModel.InvalidRdf,
                ResourceFactory.createResource(uri),
                null);
        }
    }


    /**
     * Remove the fragment portion of a URI.
     * @param uri the uri whose fragment, if any, is to be removed
     * @return the uri minus any fragment part
     * @throws URISyntaxException if the URI is invalid
     */
    public URI removeFragment(String uri) throws URISyntaxException
    {
        URI httpUri = new URI(uri);
        StringBuilder sb = new StringBuilder();
        sb.append(httpUri.getScheme());
        sb.append("://");
        sb.append(httpUri.getHost());
        if (httpUri.getPort() > 0)
        {
            sb.append(":");
            sb.append(httpUri.getPort());
        }
        sb.append(httpUri.getPath());
        return new URI(sb.toString());
    }


    private void fetchHttpResource(URI httpUri) throws ShapeCheckException, IOException
    {
        if (foundHttpResources.containsKey(httpUri))
        {
            // Resource previously found
        }
        else if (skipURIs.contains(httpUri.toString()))
        {
            // Do not try to read or parse this
            foundHttpResources.put(httpUri, false);
        }
        else if (!isRDF(httpUri))
        {
            // Resource is present, but is not RDF
            foundHttpResources.put(httpUri, false);
        }
        else
        {
           // Resource is RDF, so read it and save contained subjects
            foundHttpResources.put(httpUri, true);
            if (verbose) System.err.println("Parsing "+httpUri.toString());
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


    private Resource findRDFResource(URI httpUri, String uri)
    {
        if (httpUri.toString().equals(uri) || foundHttpResources.get(httpUri) == false)
        {
            // The HTTP resource itself has already been found
            // We do not check non-RDF hash resources
            return null;
        }
        else if (foundRDFResources.contains(uri))
        {
            // RDF Resource has been found
            return null;
        }
        else
        {
            return ResultModel.Unreachable;
        }
    }


    private boolean isRDF(URI httpUri) throws ShapeCheckException, IOException
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(httpUri);
        get.addHeader("Accept", JENA_RDF_ACCEPT_HEADER);
        HttpResponse response = httpClient.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200)
        {
            foundHttpResources.put(httpUri, false);
            throw new ShapeCheckException(
                ResultModel.InvalidRdf,
                ResourceFactory.createResource(httpUri.toString()),
                ResourceFactory.createTypedLiteral(new Integer(statusCode)));
        }
        Header[] contentTypes = response.getHeaders("Content-Type");
        for (Header contentType : contentTypes)
        {
            if (contentType.getValue().matches(".*("+RDF_TYPES+").*"))
            {
                return true;
            }
        }
        return false;
    }
}
