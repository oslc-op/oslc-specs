package net.open_services.scheck.util;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import static net.open_services.scheck.util.PrintUtils.prefix;

/**
 * Print a Turtle-appropriate representation of an RDF node.
 *
 * @author Nick Crossley. Released to public domain 2020.
 */
public class TurtlePrintVisitor implements RDFVisitor
{
    @Override
    public String visitBlank(Resource r, AnonId id)
    {
        throw new UnsupportedOperationException("RDFVisitor.visitBlank not yet implemented");
    }

    @Override
    public String visitURI(Resource r, String uri)
    {
        return prefix(uri);
    }

    @Override
    public String visitLiteral(Literal l)
    {
        RDFDatatype dt = l.getDatatype();

        // Plain literals are strings
        if (dt == null || dt.equals(XSDDatatype.XSDstring))
        {
            return enquote(l);
        }
        else if (dt.getURI().equals(RDF.langString.getURI()))
        {
            String lang = l.getLanguage();
            return enquote(l) + (lang.isEmpty() ? "" : "@"+lang);
        }
        else if (dt.equals(RDF.dtXMLLiteral))
        {
            return enquote(l) + "^^rdf:XMLLiteral";
        }
        else
        {
            return l.getLexicalForm();
        }
    }

    private static String enquote(Literal l)
    {
        return "\""+l.getString()
            .replace("\\", "\\\\")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\f", "\\f")
            .replace("\"", "\\\"")
            + "\"";
    }

}
