package net.open_services.scheck.shapechecker;

import java.util.function.Function;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Check the properties of one node in a model.
 * @author Nick Crossley. Released to public domain.
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
     * Check the validity of an object that can be either a literal or a URI
     * @param property the property whose existence should be checked
     * @param occurs the valid occurrences for the property
     * @param validator a function to perform extra validation
     * @return the number of errors noted in this check
     */
    @javax.annotation.CheckReturnValue
    public int checkNode(Property property, Occurrence occurs, Function<String,Resource> validator)
    {
        int errCount = 0;
        int count = 0;

        StmtIterator it = originalModel.listStatements(subject,property,(RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            count++;

            RDFNode node = st.getObject();
            Resource validation;

            // Custom validation?
            if (validator != null && (validation = validator.apply(node.toString())) != null)
            {
                resultModel.createIssue(resultNode, validation, property, node);
                errCount++;
            }
            shrinkingModel.remove(st);
        }
        if (count == 0 && !occurs.isOptional())
        {
            resultModel.createIssue(resultNode, ResultModel.Missing, property);
            errCount++;
        }
        else if (count > 1 && !occurs.allowMultiple())
        {
            resultModel.createIssue(resultNode, ResultModel.MoreThanOne, property);
            errCount++;
        }
        return errCount;
    }


    /**
     * Check the validity of a literal
     * @param property the property whose literal values should be checked
     * @param datatype the type of the literal
     * @param occurs the valid occurrences for the property
     * @param validator a function to perform extra validation
     * @return the number of errors noted in this check
     */
    @javax.annotation.CheckReturnValue
    public int checkLiteral(Property property, RDFDatatype datatype, Occurrence occurs, Function<String,Resource> validator)
    {
        int errCount = 0;
        int count = 0;

        StmtIterator it = originalModel.listStatements(subject,property,(RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            count++;

            RDFNode node = st.getObject();

            if (!node.isLiteral())
            {
                resultModel.createIssue(resultNode, ResultModel.NotLiteral, property, node);
                errCount++;
            }
            else
            {
                Literal lit = node.asLiteral();
                RDFDatatype dt = lit.getDatatype();
                Resource validation;

                if (datatype != null && !datatype.equals(XSDDatatype.XSDstring))
                {
                    // Literal of type other than some string
                    if (!datatype.equals(dt))
                    {
                        resultModel.createIssue(resultNode, ResultModel.WrongType, property, node);
                        errCount++;
                    }
                }
                else
                {
                    // String literal
                    if (dt != null && !dt.equals(XSDDatatype.XSDstring)
                        && !(dt.equals(RDF.dtXMLLiteral) && lit.isWellFormedXML()))
                    {
                        resultModel.createIssue(resultNode, ResultModel.WrongType, property, node);
                        errCount++;
                    }
                }

                // Custom validation?
                if (validator != null && (validation = validator.apply(lit.getLexicalForm())) != null)
                {
                    resultModel.createIssue(resultNode, validation, property, node);
                    errCount++;
                }
            }
            shrinkingModel.remove(st);
        }
        if (count == 0 && !occurs.isOptional())
        {
            resultModel.createIssue(resultNode, ResultModel.Missing, property);
            errCount++;
        }
        else if (count > 1 && !occurs.allowMultiple())
        {
            resultModel.createIssue(resultNode, ResultModel.MoreThanOne, property);
            errCount++;
        }
        return errCount;
    }


    /**
     * Check the validity of a uri
     *
     * @param property the property whose uri values should be checked
     * @param occurs the valid occurrences for the property
     * @param validator a function to perform extra validation
     * @return the number of errors noted in this check
     */
    @javax.annotation.CheckReturnValue
    public int checkURI(Property property, Occurrence occurs, Function<String,Resource> validator)
    {
        int errCount = 0;
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
                resultModel.createIssue(resultNode, ResultModel.NotResource, property, node);
                errCount++;
                shrinkingModel.remove(st);
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
                        errCount++;
                    }
                }

                if (validator != null && (validation = validator.apply(uri)) != null)
                {
                    resultModel.createIssue(resultNode, validation, property, node);
                    errCount++;
                }
                shrinkingModel.remove(st);
            }
        }
        if (count == 0 && !occurs.isOptional())
        {
            resultModel.createIssue(resultNode, ResultModel.Missing, property);
            errCount++;
        }
        else if (count > 1 && !occurs.allowMultiple())
        {
            resultModel.createIssue(resultNode, ResultModel.MoreThanOne, property);
            errCount++;
        }
        return errCount;
    }
}
