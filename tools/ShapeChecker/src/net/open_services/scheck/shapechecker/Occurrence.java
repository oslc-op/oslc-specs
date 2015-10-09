package net.open_services.scheck.shapechecker;

import java.util.HashSet;
import java.util.Set;

/**
 * Models the OSLC Core property oslc:occurs, allowing values
 * {@code oslc:Exactly-one}, {@code oslc:One-or-many}, {@code oslc:Zero-or-one},
 * or {@code oslc:Zero-or-many}.
 *
 * @author Nick Crossley. Released to public domain.
 */
public enum Occurrence
{
    /** {@code oslc:Exactly-one} */
    ExactlyOne("http://open-services.net/ns/core#Exactly-one", false, false),

    /** {@code oslc:One-or-many} */
    OneOrMany("http://open-services.net/ns/core#One-or-many", false, true),

    /** {@code oslc:Zero-or-one} */
    ZeroOrOne("http://open-services.net/ns/core#Zero-or-one", true, false),

    /** {@code oslc:Zero-or-many} */
    ZeroOrMany("http://open-services.net/ns/core#Zero-or-many", true, true);

    private String  uri;
    private boolean isOptional;
    private boolean allowMultiple;

    private static Set<String> uris = new HashSet<>();

    static
    {
        for (Occurrence occurrence : Occurrence.values())
        {
            uris.add(occurrence.getUri());
        }
    }


    Occurrence(String uri, boolean isOptional, boolean allowMultiple)
    {
        this.uri = uri;
        this.isOptional = isOptional;
        this.allowMultiple = allowMultiple;
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


    /**
     * Get the isOptional.
     *
     * @return returns the isOptional.
     */
    public boolean isOptional()
    {
        return isOptional;
    }


    /**
     * Get the allowMultiple.
     *
     * @return returns the allowMultiple.
     */
    public boolean allowMultiple()
    {
        return allowMultiple;
    }


    /**
     * Check if a given URI is a valid occurrence
     * @param checkURI the URI to check
     * @return true if the given URI is a valid occurrence
     */
    public static boolean isValidURI(String checkURI)
    {
        return uris.contains(checkURI);
    }
}
