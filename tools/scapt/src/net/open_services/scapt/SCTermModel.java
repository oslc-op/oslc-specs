package net.open_services.scapt;

import net.open_services.scheck.annotations.IssueSeverity;

/**
 * Data object for ShapeChecker vocabulary term.
 * @author Nick Crossley. Released to public domain, September 2015.
 */
public class SCTermModel
{
    private String name;
    private String description;
    private IssueSeverity severity;


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
}
