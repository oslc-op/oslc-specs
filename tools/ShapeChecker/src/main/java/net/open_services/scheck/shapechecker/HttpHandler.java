package net.open_services.scheck.shapechecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;


/**
 * Handle http connections, and verify the reachability of URIs.
 * @author Nick Crossley. Released to public domain 2015.
 */
public class HttpHandler
{
    private static final String TEXT_CONTENT_TYPES = "text/html;text/*;*/*";
    private static final String RDF_CONTENT_TYPES  = "text/turtle;application/n-triples;application/rdf+xml;application/ld+json;text/*";


    private Map<URI, Boolean> httpResourceIsRDF = new HashMap<>();
    private Set<String>       foundRDFResources = new HashSet<>();
    private Set<Pattern>      skipURIPatterns   = new HashSet<>();
    private int                 debug             = 0;
    private HttpClient        httpClient;
    private final HttpClient  rdfClient;


    /**
     * Construct a new HttpHandler that will redirect.
     * @param debug the debug level for logging
     * @param skipURIPatterns patterns of URIs not to be fetched
     */
    public HttpHandler(int debug, Set<Pattern> skipURIPatterns)
    {
        this.debug = debug;
        if (skipURIPatterns != null)
        {
            this.skipURIPatterns.addAll(skipURIPatterns);
        }

        Header header = new BasicHeader(HttpHeaders.ACCEPT, TEXT_CONTENT_TYPES);
        httpClient = HttpClientBuilder
                .create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultHeaders(Collections.singletonList(header))
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .build();

        // Seems like Jena has a bug of ignoring the RDFParserBuilder Accept header,
        // and Dublin Core uses an arcane set of redirects including 308, not handled by Apache by default,
        // so we need to configure our HttpClient very carefully!
        Header rdfHeader = new BasicHeader(HttpHeaders.ACCEPT, RDF_CONTENT_TYPES);
        HttpClientBuilder builder = HttpClientBuilder
            .create()
            .setRedirectStrategy(redirect308())
            .setDefaultHeaders(Collections.singletonList(rdfHeader))
            .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> request.addHeader(HttpHeaders.ACCEPT, RDF_CONTENT_TYPES));

        if (debug > 2)
        {
            // for debugging redirects
            System.err.println("Setting up http interceptor");
            builder = builder.addInterceptorFirst((HttpRequestInterceptor) (response, context) -> System.err.println(response.toString()))
                .addInterceptorLast(this::responseInterceptor);
        }
        rdfClient = builder.build();
    }


    //CSOFF AnonInnerLength
    private static RedirectStrategy redirect308()
    {
        return new DefaultRedirectStrategy()
            {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
                {
                    int statusCode = response.getStatusLine().getStatusCode();
                    switch (statusCode)
                    {
                    case 301:
                    case 302:
                    case 303:
                    case 307:
                    case 308:
                        return true;
                    case 304:
                    case 305:
                    case 306:
                    default:
                        return false;
                    }
                }


                @Override
                public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
                        throws ProtocolException
                {
                    URI uri = this.getLocationURI(request, response, context);
                    String method = request.getRequestLine().getMethod();
                    if (method.equalsIgnoreCase("HEAD"))
                    {
                        return new HttpHead(uri);
                    }
                    else if (method.equalsIgnoreCase("GET"))
                    {
                        return new HttpGet(uri);
                    }
                    else
                    {
                        int status = response.getStatusLine().getStatusCode();
                        HttpUriRequest toReturn = null;
                        if (status == 307 || status == 308)
                        {
                            toReturn = RequestBuilder.copy(request).setUri(uri).build();
                            // Workaround for an apparent bug in HttpClient
                            toReturn.removeHeaders("Content-Length");
                        }
                        else
                        {
                            toReturn = new HttpGet(uri);
                        }
                        return toReturn;
                    }
                }
            };
    }


    private void responseInterceptor(HttpResponse response, @SuppressWarnings("unused") HttpContext context) throws IOException
    {
        HttpEntityWrapper wrapper = new BufferedHttpEntity(response.getEntity());
        response.setEntity(wrapper);

        // Print response code
        System.err.println(response.getStatusLine());

        // Print http response headers
        for (Header hdr : response.getAllHeaders())
        {
            System.err.printf("%s: %s%n", hdr.getName(), hdr.getValue());
        }

        // Print http response content
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(wrapper.getContent())))
        {
            System.err.println(rdr.lines().collect(Collectors.joining("\n")));
        }
    }


    private RDFParserBuilder builderFactory()
    {
        return RDFParserBuilder.create()
            .httpClient(rdfClient)
            .httpAccept(RDF_CONTENT_TYPES)
            .lang(Lang.TURTLE); // *default* lang
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


    @javax.annotation.CheckReturnValue
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
        URI httpUri = httpUriOrig;

        if (httpResourceIsRDF.containsKey(httpUri))
        {
            // Resource previously found
        }
        else if (containsMatch(skipURIPatterns,httpUriOrig.toString()))
        {
            // Do not try to read or parse this
            if (debug > 1)
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
            if (debug > 0)
            {
                System.err.println("Parsing "+httpUri);
            }
            Model foundModel = ModelFactory.createDefaultModel();

            RDFParserBuilder rdfParserBuilder = builderFactory();
            String source = httpUri.toString();
            rdfParserBuilder = rdfParserBuilder.source(source);
            RDFParser parser = rdfParserBuilder.build();
            parser.parse(foundModel);

            ResIterator ri = foundModel.listSubjects();
            while (ri.hasNext())
            {
                // Skip any blank node subjects
                String uri = ri.next().getURI();
                if (uri != null)
                {
                    if (debug > 1)
                    {
                        System.err.println("Adding RDF resource "+uri);
                    }
                    foundRDFResources.add(uri);
                }
            }
        }
    }


    private void fetchPlainHttpResource(URI httpUri) throws ShapeCheckException, IOException
    {
        if (containsMatch(skipURIPatterns,httpUri.toString()))
        {
            // Do not try to read or parse this
            if (debug > 1)
            {
                System.err.println("Skipping reference check for "+httpUri);
            }
            httpResourceIsRDF.put(httpUri, false);
            return;
        }

        HttpGet get = new HttpGet(httpUri);
        if (debug > 1)
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
            if (debug > 1)
            {
                System.err.println("Cannot find RDF resource "+uri);
            }
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
