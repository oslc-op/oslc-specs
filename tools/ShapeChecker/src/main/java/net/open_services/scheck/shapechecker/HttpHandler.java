package net.open_services.scheck.shapechecker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.ResourceFactory;


/**
 * Handle http connections, and verify the reachability of URIs.
 * @author Nick Crossley. Released to public domain 2015.
 */
public class HttpHandler
{
    private Map<URI, Boolean> httpResourceIsRDF = new HashMap<>();
    private Set<String>       foundRDFResources = new HashSet<>();
    private Set<Pattern>      skipURIPatterns   = new HashSet<>();
    private boolean           debug             = false;
    private HttpClient        httpClient;


    /**
     * Construct a new HttpHandler that will redirect.
     */
    public HttpHandler()
    {
        Header header = new BasicHeader(HttpHeaders.ACCEPT, "text/html;text/*;*/*");
        httpClient = HttpClientBuilder
                .create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultHeaders(Collections.singletonList(header))
                .build();
    }

    /**
     * Sets the debug option.
     * @param debug the debug value to set.
     */
    public void setDebug(boolean debug)
    {
        this.debug = debug;
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


    /**
     * Exclude a URI pattern from being fetched and parsed.
     * @param uri the URI pattern to be skipped
     */
    public void excludeURIPattern(String uri)
    {
        skipURIPatterns.add(Pattern.compile(uri));
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


    private void fetchRdfHttpResource(URI httpUriOrig)
    {
        URI httpUri = dctermsRedirectWorkaround(httpUriOrig);

        if (httpResourceIsRDF.containsKey(httpUri))
        {
            // Resource previously found
        }
        else if (containsMatch(skipURIPatterns,httpUriOrig.toString()))
        {
            // Do not try to read or parse this
            if (debug)
            {
                System.err.println("Skipping reference check for "+httpUri);
            }
            httpResourceIsRDF.put(httpUri, false);
        }
        else
        {
            // Resource might be RDF, so try to read it and save contained subjects
            // An exception will be thrown by Jena if the resource is not RDF, and the
            // setting below will be reversed.
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


    /**
     * Hack to work around <a href="https://github.com/oslc-op/oslc-specs/issues/88">issue88</a>
     * with Jena handling of Dublin Core redirects.
     * @param httpUriOrig a URI
     * @return if the input was a DCTerms URI, return a modified URI reflecting the several redirects
     */
    private static URI dctermsRedirectWorkaround(URI httpUriOrig)
    {
        Pattern dcterms = Pattern.compile("^http://purl\\.org/dc/terms/(.*)$");
        URI httpUri = httpUriOrig;
        Matcher m = dcterms.matcher(httpUri.toString());

        if (m.matches())
        {
            httpUri = URI.create("https://www.dublincore.org/2012/06/14/dcterms.rdf#" + m.group(1));
        }
        return httpUri;
    }


    private void fetchPlainHttpResource(URI httpUri) throws ShapeCheckException, IOException
    {
        if (containsMatch(skipURIPatterns,httpUri.toString()))
        {
            // Do not try to read or parse this
            if (debug)
            {
                System.err.println("Skipping reference check for "+httpUri);
            }
            httpResourceIsRDF.put(httpUri, false);
            return;
        }

        HttpGet get = new HttpGet(httpUri);
        if (debug)
        {
            System.err.println("Fetching " + httpUri);
        }
        try
        {
            HttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200)
            {
                httpResourceIsRDF.put(httpUri, false);
                throw new ShapeCheckException(
                    Terms.Unreachable,
                    ResourceFactory.createResource(httpUri.toString()),
                    ResourceFactory.createTypedLiteral(Integer.valueOf(statusCode)));
            }
        }
        finally
        {
            get.reset();
        }
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


    /**
     * Check to see if a URI is reachable.
     * @param uri the uri to check
     * @param shouldBeRdf true if the target should be RDF
     * @throws ShapeCheckException if there was a problem checking the URI,
     * or if it should have been RDF but was not.
     */
    public void checkValidReference(String uri, boolean shouldBeRdf) throws ShapeCheckException
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
            if (shouldBeRdf)
            {
                fetchRdfHttpResource(httpUri);
                findRDFResource(httpUri,uri);
            }
            else
            {
                fetchPlainHttpResource(httpUri);
            }
        }
        catch (IOException | org.apache.jena.atlas.web.HttpException e1)
        {
            httpResourceIsRDF.put(httpUri, false);
            throw new ShapeCheckException(
                Terms.Unreachable,
                ResourceFactory.createResource(uri),
                ResourceFactory.createResource(e1.toString()),
                e1);
        }
        catch (org.apache.jena.shared.JenaException e2)
        {
            httpResourceIsRDF.put(httpUri, false);
            if (shouldBeRdf)
            {
                throw new ShapeCheckException(
                    Terms.InvalidRdfError,
                    ResourceFactory.createResource(uri),
                    ResourceFactory.createResource(e2.toString()),
                    e2);
            }
        }

    }
}
