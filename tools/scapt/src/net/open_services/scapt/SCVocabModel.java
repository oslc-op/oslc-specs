package net.open_services.scapt;

/**
 * Data object for ShapeChecker vocabulary.
 * @author Nick Crossley. Released to public domain, September 2015.
 */
public class SCVocabModel
{
    private String uri;
    private String prefix;
    private String domain;
    private String description;


    /**
     * Construct a new SCVocabModel.
     * @param uri the full URI for the vocabulary
     * @param prefix the preferred namespace prefix for the vocabulary
     * @param domain a short title for the vocabulary/domain
     * @param description a longer description of the vocabulary or domain
     */
    public SCVocabModel(String uri, String prefix, String domain, String description)
    {
        this.uri = uri;
        this.prefix = prefix;
        this.domain = domain;
        this.description = description;
    }


    /**
     * Get the URI.
     * @return returns the URI.
     */
    public String getUri()
    {
        return uri;
    }


    /**
     * Get the prefix.
     * @return returns the prefix.
     */
    public String getPrefix()
    {
        return prefix;
    }


    /**
     * Get the domain.
     * @return returns the domain.
     */
    public String getDomain()
    {
        return domain;
    }


    /**
     * Get the description.
     * @return returns the description.
     */
    public String getDescription()
    {
        return description;
    }
}
