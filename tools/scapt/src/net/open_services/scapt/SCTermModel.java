package net.open_services.scapt;

import net.open_services.scheck.annotations.IssueSeverity;

/**
 * Data object for ShapeChecker vocabulary term.
 * @author Nick Crossley. Released to public domain, September 2015.
 */
public class SCTermModel
{
    private String        name;
    private String        description;
    private IssueSeverity severity;
    private String        singular;
    private String        plural;


    /**
     * Construct a new SCTerm.
     * @param name the name of the vocabulary term
     * @param description a description of the vocabulary term
     */
    public SCTermModel(String name, String description)
    {
        this.name = name;
        this.description = description;
    }


    /**
     * Construct a new SCTerm for an issue with a severity.
     * @param name the name of the issue
     * @param description a description of the issue
     * @param issueSeverity the severity of the issue
     */
    public SCTermModel(String name, String description, IssueSeverity issueSeverity)
    {
        this.name = name;
        this.description = description;
        this.severity = issueSeverity;
    }


    /**
     * Construct a new SCTerm for an issue with a severity.
     * @param name the name of the issue
     * @param description a description of the issue
     * @param severity the severity of the issue
     * @param singular the singular for a cross-check artifact
     * @param plural the plural for a cross-check artifact
     */
    public SCTermModel(String name, String description, IssueSeverity severity, String singular, String plural)
    {
        this.name = name;
        this.description = description;
        this.severity = severity;
        this.singular = singular;
        this.plural = plural;
    }


    /**
     * Get the name.
     * @return returns the name.
     */
    public String getName()
    {
        return name;
    }


    /**
     * Get the description.
     * @return returns the description.
     */
    public String getDescription()
    {
        return description;
    }


    /**
     * Get the severity.
     * @return returns the severity.
     */
    public IssueSeverity getSeverity()
    {
        return severity;
    }


    /**
     * Get the singular.
     * @return returns the singular.
     */
    public String getSingular()
    {
        return singular;
    }


    /**
     * Get the plural.
     * @return returns the plural.
     */
    public String getPlural()
    {
        return plural;
    }
}
