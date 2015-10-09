package net.open_services.scheck.shapechecker;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Check the statements in a single OSLC Shape.
 * @author Nick Crossley. Released to public domain.
 */
public class ShapeCheck
{
    private HttpHandler httpHandler;
    private Model       shapeModel;
    private Model       shapeCopy;
    private ResultModel resultModel;
    private Resource    shapeResult;
    private Set<String> describes;
    private Set<String> names;
    private Set<String> predicates;


    /**
     * Construct a new ShapeCheck.
     * @param describes a set of shape names already seen
     * @param httpHandler an HttpHandler to check for reachability of URI references
     * @param shapeModel a model containing the shape statements
     * @param shapeCopy a reducing model containing as-yet unprocessed statements about the shape
     * @param resultModel the model to which any results should be added
     * @param shapesResult a node in the resultModel for the results for all shapes in a document
     */
    public ShapeCheck(Set<String> describes, HttpHandler httpHandler, Model shapeModel, Model shapeCopy, ResultModel resultModel, Resource shapesResult)
    {
        this.describes = describes;
        this.httpHandler = httpHandler;
        this.shapeModel = shapeModel;
        this.shapeCopy = shapeCopy;
        this.resultModel = resultModel;
        shapeResult = resultModel.createInnerResult(shapesResult, ResultModel.ShapeResult);

        names = new HashSet<>();
        predicates = new HashSet<>();
    }


    /**
     * Perform a number of consistency checks on a single shape in the model.
     * @param document the URI of the document containing this shape
     * @param shape the shape resource to be checked
     * @return the number of errors found
     */
    @javax.annotation.CheckReturnValue
    public int checkShape(URI document, Resource shape)
    {
        int errors = 0;

        // Look for the required and optional literal properties
        NodeCheck node = new NodeCheck(shape, httpHandler, shapeModel, shapeCopy, resultModel, shapeResult);
        errors += node.checkLiteral(DCTerms.title, null, Occurrence.ExactlyOne, null);
        errors += node.checkLiteral(DCTerms.description, null, Occurrence.ZeroOrOne, null);
        errors += node.checkURI(OSLC.describes, Occurrence.ExactlyOne,
            (uri)->{shapeResult.addProperty(ResultModel.checks, ResourceFactory.createResource(uri));return checkUnique(describes,uri);});
        errors += node.checkURI(RDFS.seeAlso, Occurrence.ZeroOrMany, null);

        StmtIterator it = shapeCopy.listStatements(shape, OSLC.property, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            it.remove();
            errors += checkPropDef(document, shape,st.getObject());
        }

        it = shapeCopy.listStatements(shape, null, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(shapeResult, ResultModel.Redundant, st.getPredicate(), st.getObject());
            it.remove();
            errors++;
        }

        return errors;
    }


    /**
     * Check the properties of one property definition in a shape
     *
     * @param document the URI of the document containing this shape
     * @param shape the shape in which the property appears
     * @param propDefNode the property definition to be checked
     * @return the number of errors found
     */
    @javax.annotation.CheckReturnValue
    private int checkPropDef(URI document, Resource shape, RDFNode propDefNode)
    {
        int errors = 0;
        Resource propDef = null;
        final Resource propResult = resultModel.createInnerResult(shapeResult, ResultModel.PropertyResult);

        // Check the property definition is a hash resource with the same base as the shape
        if (propDefNode.isLiteral())
        {
            errors++;
            resultModel.createIssue(propResult, ResultModel.NotResource, propDefNode.asLiteral().getLexicalForm());
            return errors;
        }
        else if (!(propDef=propDefNode.asResource()).isAnon() &&
                !propDef.getURI().replaceAll("#[^/]+$","").equals(shape.getURI().replaceAll("#[^/]+$","")) &&
                !propDef.getURI().replaceAll("#[^/]+$","").equals(document.toString()
                    .replaceAll("#[^/]+$","")
                    .replaceAll("file:/(?!/)", "file:///")))
        {
            errors++;
            resultModel.createIssue(propResult, ResultModel.NotHash, propDefNode.asResource());
        }

        // Check the mandatory properties of the property definition
        NodeCheck node = new NodeCheck(propDef, httpHandler, shapeModel, shapeCopy, resultModel, propResult);
        errors += node.checkLiteral(DCTerms.description, null, Occurrence.ExactlyOne, null);
        errors += node.checkLiteral(OSLC.name, null, Occurrence.ExactlyOne,
            (literal) -> (checkUnique(names,literal)));
        errors += node.checkURI(OSLC.occurs, Occurrence.ExactlyOne,
            (uri) -> (Occurrence.isValidURI(uri) ? null : ResultModel.BadOccurs));
        errors += node.checkURI(OSLC.propertyDefinition, Occurrence.ExactlyOne,
            (uri) -> {propResult.addProperty(ResultModel.checks, ResourceFactory.createResource(uri));return checkUnique(predicates,uri);});
        errors += node.checkURI(RDF.type, Occurrence.ExactlyOne,
            (uri) -> (uri.equals(OSLC.Property.getURI()) ? null : ResultModel.WrongType));

        // Check the optional properties of the property definition
        errors += node.checkLiteral(DCTerms.title, null, Occurrence.ZeroOrOne, null);
        errors += node.checkLiteral(OSLC.readOnly, XSDDatatype.XSDboolean, Occurrence.ZeroOrOne, null);
        errors += node.checkLiteral(OSLC.hidden, XSDDatatype.XSDboolean, Occurrence.ZeroOrOne, null);
        errors += node.checkURI(OSLC.allowedValue, Occurrence.ZeroOrMany,
            (uri) -> {propResult.addProperty(DCTerms.references, ResourceFactory.createResource(uri)); return null;});
        errors += node.checkURI(OSLC.allowedValues, Occurrence.ZeroOrOne, null); // TODO - validate the allowedValues resource
        errors += node.checkURI(OSLC.valueShape, Occurrence.ZeroOrOne, null); // TODO - add this shape to a list of shapes that must be defined
        errors += node.checkURI(OSLC.range, Occurrence.ZeroOrMany, null);
        errors += node.checkNode(OSLC.defaultValue, Occurrence.ZeroOrOne,
            (uri) -> {propResult.addProperty(DCTerms.references, ResourceFactory.createResource(uri)); return null;});

        errors += checkValueType(propDef,propResult);

        // Check that the prop def has no other properties
        StmtIterator it = shapeCopy.listStatements(propDef, null, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(propResult, ResultModel.Redundant, st.getPredicate());
            it.remove();
            errors++;
        }

        return errors;
    }


    /**
     * Check that a property has a value unique to the appropriate scope.
     * @param valueSet the scope set of the values to be checked
     * @param value the value of the property to be checked
     * @return the duplicate name error class if the name is not unique,
     * or null if the name is unique
     */
    public Resource checkUnique(Set<String> valueSet,String value)
    {
        if (valueSet.contains(value))
        {
            return ResultModel.Duplicate;
        }
        else
        {
            valueSet.add(value);
            return null;
        }
    }


    /**
     * Special checks for oslc:valueType property
     *
     * @param propDef the property definition whose type is being checked
     * @param propResult the {@link ResultModel} resource to which to add errors
     * @return the number of errors found
     */
    @javax.annotation.CheckReturnValue
    public int checkValueType(Resource propDef,Resource propResult)
    {
        int errCount = 0;
        Resource valueType = OSLC.Any;
        StmtIterator it = shapeModel.listStatements(propDef, OSLC.valueType, (RDFNode)null);
        if (it.hasNext())
        {
            RDFNode tnode;
            Statement st = it.next();
            shapeCopy.remove(st);
            if (!(tnode = st.getObject()).isResource())
            {
                resultModel.createIssue(propResult, ResultModel.NotResource, OSLC.valueType, tnode);
                errCount++;
                it.remove();
            }
            else
            {
                valueType = tnode.asResource();
                it.remove();
            }
        }
        if (it.hasNext())
        {
            resultModel.createIssue(propResult, ResultModel.MoreThanOne, OSLC.valueType);
            errCount++;
        }

        NodeCheck node = new NodeCheck(propDef, httpHandler, shapeModel, shapeCopy, resultModel, propResult);
        final Resource finalValueType = valueType;
        if (valueType.equals(XSD.xboolean) || valueType.equals(XSD.dateTime) || valueType.equals(XSD.decimal)
                || valueType.equals(XSD.xfloat) || valueType.equals(XSD.xdouble) || valueType.equals(XSD.integer)
                || valueType.equals(XSD.xstring) || valueType.equals(RDF.xmlLiteral))
        {
            // No further checks
        }
        else if (valueType.equals(OSLC.Any))
        {
            // No further checks
        }
        else if (valueType.equals(OSLC.LocalResource))
        {
            resultModel.createIssue(propResult, ResultModel.LocalResourceDeprecated, OSLC.valueType);
            errCount++;
            errCount += node.checkURI(OSLC.representation, Occurrence.ExactlyOne,
                (uri) -> {
                    Resource res = ResourceFactory.createResource(uri);
                    return res.equals(OSLC.Inline) ? null : ResultModel.MismatchingRepresentation;});
        }
        else if (valueType.equals(OSLC.AnyResource) || valueType.equals(OSLC.Resource))
        {
            errCount += node.checkURI(OSLC.representation, Occurrence.ExactlyOne,
                (uri) -> {
                    Resource res = ResourceFactory.createResource(uri);
                    return (finalValueType.equals(OSLC.AnyResource) ? (res.equals(OSLC.Inline) || res.equals(OSLC.Either))
                            : (res.equals(OSLC.Inline) || res.equals(OSLC.Either) || res.equals(OSLC.Reference)))
                                ? null : ResultModel.MismatchingRepresentation;
                });
        }
        else
        {
            resultModel.createIssue(propResult, ResultModel.WrongType, OSLC.valueType, valueType);
            errCount++;
        }
        return errCount;
    }
}
