package net.open_services.scheck.shapechecker;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Check the properties of one node in a model.
 * @author Nick Crossley. Released to public domain 2015.
 */
public class NodeCheck
{
    private HttpHandler httpHandler;
    private Resource    subject;
    private Model       originalModel;
    private Model       shrinkingModel;
    private ResultModel resultModel;
    private Resource    resultNode;


    /**
     * Construct a new NodeCheck.
     * @param subject the subject resource
     * @param httpHandler an HttpHandler to check for reachability of URI references
     * @param originalModel the original read-only model that contains the triple being checked
     * @param shrinkingModel the model being reduced from the original, with checked statements removed
     * @param resultModel the {@link ResultModel} to which results and errors are being added
     * @param resultNode the node in the result model to which results and errors are being added
     */
    public NodeCheck(Resource subject, HttpHandler httpHandler, Model originalModel, Model shrinkingModel,
            ResultModel resultModel, Resource resultNode)
    {
        this.subject = subject;
        this.httpHandler = httpHandler;
        this.originalModel = originalModel;
        this.shrinkingModel = shrinkingModel;
        this.resultModel = resultModel;
        this.resultNode = resultNode;
    }


    /**
     * Check the validity of an object that can be either a literal or a URI.
     * @param property the property whose existence should be checked
     * @param occurs the valid occurrences for the property
     */
    public void checkNode(Property property, Occurrence occurs)
    {
        checkNode(property,occurs,null,null);
    }


    /**
     * Check the validity of an object that can be either a literal or a URI.
     * @param property the property whose existence should be checked
     * @param occurs the valid occurrences for the property
     * @param literalValidator a function to perform extra validation for a literal property value
     * @param uriValidator a function to perform extra validation for a URI property value
     */
    public void checkNode(Property property, Occurrence occurs, Function<String,Resource> literalValidator, Function<String,Resource> uriValidator)
    {
        int count = 0;

        StmtIterator it = originalModel.listStatements(subject,property,(RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            count++;

            RDFNode node = st.getObject();
            Resource validation;

            // Custom validation?
            if (node.isLiteral())
            {
                if (literalValidator != null && (validation = literalValidator.apply(node.toString())) != null)
                {
                    resultModel.createIssue(resultNode, validation, property, node);
                }
            }
            else
            {
                if (uriValidator != null && (validation = uriValidator.apply(node.toString())) != null)
                {
                    resultModel.createIssue(resultNode, validation, property, node);
                }
            }
            shrinkingModel.remove(st);
        }
        if (count == 0 && !occurs.isOptional())
        {
            resultModel.createIssue(resultNode, Terms.MissingError, property);
        }
        else if (count > 1 && !occurs.allowMultiple())
        {
            resultModel.createIssue(resultNode, Terms.MoreThanOne, property);
        }
    }


    /**
     * Check the validity of a literal.
     * @param property the property whose literal values should be checked
     * @param datatype the type of the literal
     * @param occurs the valid occurrences for the property
     * @param validator a function to perform extra validation
     */
    public void checkLiteral(Property property, RDFDatatype datatype, Occurrence occurs, Function<String,Resource> validator)
    {
        int count = 0;

        StmtIterator it = originalModel.listStatements(subject,property,(RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            count++;

            RDFNode node = st.getObject();

            if (!node.isLiteral())
            {
                resultModel.createIssue(resultNode, Terms.NotLiteral, property, node);
            }
            else
            {
                Literal lit = node.asLiteral();
                RDFDatatype dt = lit.getDatatype();
                Resource validation;

                // Plain literals are strings
                if (dt == null)
                {
                    dt = XSDDatatype.XSDstring;
                }

                // Check valid XML strings
                if (dt.equals(RDF.dtXMLLiteral) && !lit.isWellFormedXML())
                {
                    resultModel.createIssue(resultNode, Terms.BadXMLLiteral, property, node);
                }

                // Check literal type matches - but consider XMLLiteral as a string type
                if (datatype != null && !(datatype.equals(dt)
                        || (datatype.equals(RDF.dtXMLLiteral) && dt.equals(XSDDatatype.XSDstring))
                        || (datatype.equals(XSDDatatype.XSDstring) && dt.equals(RDF.dtXMLLiteral))))
                {
                    resultModel.createIssue(resultNode, Terms.WrongType, property, node);
                }

                // Custom validation?
                if (validator != null && (validation = validator.apply(lit.getLexicalForm())) != null)
                {
                    resultModel.createIssue(resultNode, validation, property, node);
                }
            }
            shrinkingModel.remove(st);
        }
        if (count == 0 && !occurs.isOptional())
        {
            resultModel.createIssue(resultNode, Terms.MissingError, property);
        }
        else if (count > 1 && !occurs.allowMultiple())
        {
            resultModel.createIssue(resultNode, Terms.MoreThanOne, property);
        }
    }



    /**
     * Check for string properties with language tags.
     *
     * @param property the property whose literal values should be checked
     * @param occurs the valid occurrences for the property
     * @param validator a function to perform extra validation
     */
    public void checkLangString(Property property, Occurrence occurs, Function<String,Resource> validator)
    {
        int count = 0;
        Set<String> langTags = new HashSet<>();

        StmtIterator it = originalModel.listStatements(subject,property,(RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            count++;

            RDFNode node = st.getObject();

            if (!node.isLiteral())
            {
                resultModel.createIssue(resultNode, Terms.NotLiteral, property, node);
            }
            else
            {
                Literal lit = node.asLiteral();
                RDFDatatype dt = lit.getDatatype();
                Resource validation;

                if (!(dt == null || dt.equals(RDF.dtLangString) || dt.equals(XSDDatatype.XSDstring) || dt.equals(RDF.dtXMLLiteral)))
                {
                    // Literal of type other than some string
                    resultModel.createIssue(resultNode, Terms.WrongType, property, node);
                }
                else if (dt!=null && dt.equals(RDF.dtXMLLiteral) && !lit.isWellFormedXML())
                {
                    resultModel.createIssue(resultNode, Terms.BadXMLLiteral, property, node);
                }
                else
                {
                    String lang = lit.getLanguage();
                    if (langTags.contains(lang))
                    {
                        resultModel.createIssue(resultNode, Terms.DuplicateLangString, property, node);
                    }
                    else
                    {
                        langTags.add(lang);
                    }


                    // Custom validation?
                    if (validator != null && (validation = validator.apply(lit.getLexicalForm())) != null)
                    {
                        resultModel.createIssue(resultNode, validation, property, node);
                    }
                }
            }
            shrinkingModel.remove(st);
        }
        if (count == 0 && !occurs.isOptional())
        {
            resultModel.createIssue(resultNode, Terms.MissingError, property);
        }
    }



    /**
     * Check the validity of a uri.
     *
     * @param property the property whose uri values should be checked
     * @param occurs the valid occurrences for the property
     * @param validator a function to perform extra validation
     */
    public void checkURI(Property property, Occurrence occurs, Function<String,Resource> validator)
    {
        int count = 0;

        StmtIterator it = originalModel.listStatements(subject, property, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            count++;

            RDFNode node = st.getObject();
            Resource validation;

            if (!node.isResource())
            {
                resultModel.createIssue(resultNode, Terms.NotResource, property, node);
            }
            else
            {
                String uri = node.asResource().getURI();
                if (originalModel.contains(node.asResource(), null))
                {
                    // ignore internal references for now
                }
                else
                {
                    try
                    {
                        httpHandler.checkValidReference(uri);
                    }
                    catch (ShapeCheckException e)
                    {
                        resultModel.createIssue(resultNode, e);
                    }
                }

                if (validator != null && (validation = validator.apply(uri)) != null)
                {
                    resultModel.createIssue(resultNode, validation, property, node);
                }
            }
            shrinkingModel.remove(st);
        }
        if (count == 0 && !occurs.isOptional())
        {
            resultModel.createIssue(resultNode,
                property.equals(OSLC.describes) ? Terms.MissingWarn : Terms.MissingError,
                property);
        }
        else if (count > 1 && !occurs.allowMultiple())
        {
            resultModel.createIssue(resultNode, Terms.MoreThanOne, property);
        }
    }


    /**
     * Check to see if a string ends with a period (full stop).
     * @param str the string to be checked
     * @return true iff the string ends with a period.
     */
    public static Resource checkPeriod(String str)
    {
        // If you want to allow trailing white space, use this:
        // return (str.matches(".*\\.\\s*$") ? null : Terms.MissingPeriod);

        return (str.endsWith(".") ? null : Terms.MissingPeriod);
    }
}
