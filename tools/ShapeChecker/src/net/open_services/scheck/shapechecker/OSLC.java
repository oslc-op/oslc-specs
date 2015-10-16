package net.open_services.scheck.shapechecker;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Constants for the OSLC Core vocabulary.
 * @author Nick Crossley. Released to public domain.
 */
@SuppressWarnings("javadoc")
public class OSLC
{
    private static final String uri = "http://open-services.net/ns/core#";


    /**
     * returns the URI for this schema
     * @return the URI for this schema
     */
    public static String getURI()
    {
        return uri;
    }


    private static final Resource resource(String local)
    {
        return ResourceFactory.createResource(uri + local);
    }


    private static final Property property(String local)
    {
        return ResourceFactory.createProperty(uri, local);
    }


    public static final Resource AllowedValues      = resource("AllowedValues");
    public static final Resource Any                = resource("Any");
    public static final Resource AnyResource        = resource("AnyResource");
    public static final Resource Either             = resource("Either");
    public static final Resource Inline             = resource("Inline");
    public static final Resource LocalResource      = resource("LocalResource");
    public static final Resource Property           = resource("Property");
    public static final Resource Reference          = resource("Reference");
    public static final Resource Resource           = resource("Resource");
    public static final Resource ResourceShape      = resource("ResourceShape");


    public static final Property allowedValue       = property("allowedValue");
    public static final Property allowedValues      = property("allowedValues");
    public static final Property defaultValue       = property("defaultValue");
    public static final Property describes          = property("describes");
    public static final Property hidden             = property("hidden");
    public static final Property impactType         = property("impactType");
    public static final Property inverseLabel       = property("inverseLabel");
    public static final Property isMemberProperty   = property("isMemberProperty");
    public static final Property maxSize            = property("maxSize");
    public static final Property name               = property("name");
    public static final Property occurs             = property("occurs");
    public static final Property property           = property("property");
    public static final Property propertyDefinition = property("propertyDefinition");
    public static final Property range              = property("range");
    public static final Property readOnly           = property("readOnly");
    public static final Property representation     = property("representation");
    public static final Property serviceProvider    = property("serviceProvider");
    public static final Property valueShape         = property("valueShape");
    public static final Property valueType          = property("valueType");

    public static final Property accessContext      = ResourceFactory.createProperty("http://open-services.net/ns/core/acc#accessContext");
}
