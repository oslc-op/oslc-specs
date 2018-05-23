package net.open_services.scheck.annotations;


/**
 * The type of a vocabulary term.
 * @author Nick Crossley. Released to public domain 2015.
 */
public enum TermType
{
    /** Vocabulary term for an RDF class. */
    Class("rdfs:Class"),

    /** Vocabulary term for an RDF property. */
    Property("rdf:Property"),

    /** Vocabulary term for an RDF individual. */
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
