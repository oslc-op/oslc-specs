package net.open_services.scapt;

/**
 * Data object for ShapeChecker vocabulary term.
 * @author Nick Crossley. Released to public domain, September 2015.
 */
public class SCTermModel
{
    private String name;
    private String description;


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
}
