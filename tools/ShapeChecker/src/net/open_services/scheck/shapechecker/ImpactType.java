package net.open_services.scheck.shapechecker;

import java.util.HashSet;
import java.util.Set;

/**
 * Models the OSLC Core property oslc:impactType, allowing values
 * {@code oslc:UpstreamImpact}, {@code oslc:DownstreamImpact}, {@code oslc:SymmetricImpact},
 * or {@code oslc:NoImpact}.
 *
 * @author Nick Crossley. Released to public domain.
 */
public enum ImpactType
{
    /** {@code oslc:UpstreamImpact} */
    UpstreamImpact("http://open-services.net/ns/core#UpstreamImpact"),

    /** {@code oslc:DownstreamImpact} */
    DownstreamImpact("http://open-services.net/ns/core#DownstreamImpact"),

    /** {@code oslc:SymmetricImpact} */
    SymmetricImpact("http://open-services.net/ns/core#SymmetricImpact"),

    /** {@code oslc:NoImpact} */
    NoImpact("http://open-services.net/ns/core#NoImpact");

    private String  uri;

    private static Set<String> uris = new HashSet<>();

    static
    {
        for (ImpactType occurrence : ImpactType.values())
        {
            uris.add(occurrence.getUri());
        }
    }


    ImpactType(String uri)
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
