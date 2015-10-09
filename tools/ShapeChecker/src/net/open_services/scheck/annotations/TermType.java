package net.open_services.scheck.annotations;


/**
 * The type of a vocabulary term.
 * @author Nick Crossley. Released to public domain, September 2015.
 */
@SuppressWarnings("javadoc")
public enum TermType
{
    Class("rdfs:Class"),
    Property("rdf:Property"),
    Resource("rdfs:Resource");


    private String uri;

    TermType(String uri)
    {
        this.uri = uri;
    }


    /**
     * Get the uri.
     *
     * @return returns the uri.
     */
    public String getUri()
    {
        return uri;
    }

}
