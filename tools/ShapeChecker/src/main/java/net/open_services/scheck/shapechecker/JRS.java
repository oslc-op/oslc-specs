package net.open_services.scheck.shapechecker;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

//CSOFF: JavadocVariableCheck
//CSOFF: DeclarationOrderCheck
//CSOFF: ConstantNameCheck
/**
 * Constants for the JRS vocabulary.
 * @author Nick Crossley. Released to public domain 2019.
 */
@SuppressWarnings("javadoc")
public final class JRS
{
    private static final String uri = "http://jazz.net/ns/jrs#";

    private JRS()
    {
        // No instantiation
    }

    /**
     * returns the URI for this schema.
     * @return the URI for this schema
     */
    public static String getURI()
    {
        return uri;
    }


    private static Property property(String local)
    {
        return ResourceFactory.createProperty(uri, local);
    }


    public static final Property inContainer                = property("inContainer");
    public static final Property inversePropertyDefinition  = property("inversePropertyDefinition");
    public static final Property inversePropertyLabel       = property("inversePropertyLabel");
    public static final Property superShape                 = property("superShape");
}
