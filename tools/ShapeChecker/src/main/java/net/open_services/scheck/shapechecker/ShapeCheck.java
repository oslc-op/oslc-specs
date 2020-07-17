package net.open_services.scheck.shapechecker;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

/**
 * Check the statements in a single OSLC Shape.
 * @author Nick Crossley. Released to public domain 2015.
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
     * @param describes a set of types for which we have already seen shapes
     * @param httpHandler an HttpHandler to check for reachability of URI references
     * @param shapeModel a model containing the shape statements
     * @param shapeCopy a reducing model containing as-yet unprocessed statements about the shape
     * @param resultModel the model to which any results should be added
     * @param shapesResult a node in the resultModel for the results for all shapes in a document
     */
    public ShapeCheck(Set<String> describes, HttpHandler httpHandler,
            Model shapeModel, Model shapeCopy, ResultModel resultModel, Resource shapesResult)
    {
        this.describes = describes;
        this.httpHandler = httpHandler;
        this.shapeModel = shapeModel;
        this.shapeCopy = shapeCopy;
        this.resultModel = resultModel;
        shapeResult = resultModel.createInnerResult(shapesResult, Terms.ShapeResult);

        names = new HashSet<>();
        predicates = new HashSet<>();
    }


    /**
     * Perform a number of consistency checks on a single shape in the model.
     * @param document the URI of the document containing this shape
     * @param shape the shape resource to be checked
     */
    @javax.annotation.CheckReturnValue
    public void checkShape(URI document, Resource shape)
    {
        shapeResult.addProperty(Terms.checks, shape);
        NodeCheck node = new NodeCheck(shape, httpHandler, shapeModel, shapeCopy, resultModel, shapeResult);

        // Look for the required properties
        // TODO Note that oslc:describes is not truly required, but we check for it because almost all shapes should have one.
        // Shapes that are introduced by use of oslc:valueShape need not have an oslc:describes property,
        // but this code has no way to check for that yet.
        node.checkURI(OSLC.describes, Occurrence.OneOrMany,
            uri -> {recordReference(shapeResult, uri); return checkUnique(describes,uri); });

        // Look for the optional properties
        node.checkLangString(DCTerms.title, Occurrence.ZeroOrOne, null);
        node.checkLangString(DCTerms.description, Occurrence.ZeroOrOne,
            desc -> NodeCheck.checkSentence(desc));
        node.checkLiteral(OSLC.hidden, XSDDatatype.XSDboolean, Occurrence.ZeroOrOne, null);
        node.checkSuppressibleURI(RDFS.seeAlso, Occurrence.ZeroOrMany, false, false, null);

        // Allow optional OSLC properties for any resource
        node.checkNode(OSLC.accessContext, Occurrence.ZeroOrOne, lit -> Terms.WrongType,null);
        node.checkNode(OSLC.serviceProvider, Occurrence.ZeroOrOne, lit -> Terms.WrongType,null);

        StmtIterator it = shapeCopy.listStatements(shape, OSLC.property, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            it.remove();
            checkPropDef(document, shape,st.getObject());
        }

        it = shapeCopy.listStatements(shape, null, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(shapeResult, Terms.Redundant, st.getPredicate(), st.getObject());
            it.remove();
        }
    }


    /**
     * Check the properties of one property definition in a shape.
     *
     * @param document the URI of the document containing this shape
     * @param shape the shape in which the property appears
     * @param propDefNode the property definition to be checked
     */
    @javax.annotation.CheckReturnValue
    private void checkPropDef(URI document, Resource shape, RDFNode propDefNode)
    {
        Resource propDef = null;
        final Resource propResult = resultModel.createInnerResult(shapeResult, Terms.PropertyResult);
        propResult.addProperty(Terms.checks, propDefNode);

        // Check the property definition is a hash resource with the same base as the shape
        if (propDefNode.isLiteral())
        {
            resultModel.createIssue(propResult, Terms.NotResource, propDefNode.asLiteral().getLexicalForm());
        }
        else if (!(propDef=propDefNode.asResource()).isAnon()
                && !propDef.getURI().replaceAll("#[^/]+$","").equals(shape.getURI().replaceAll("#[^/]+$",""))
                && !propDef.getURI().replaceAll("#[^/]+$","").equals(document.toString()
                    .replaceAll("#[^/]+$","")
                    .replaceAll("file:/(?!/)", "file:///")))
        {
            resultModel.createIssue(propResult, Terms.NotHash, propDefNode.asResource());
        }


        // Check the existence and mandatory properties of the property definition
        NodeCheck node = new NodeCheck(propDef, httpHandler, shapeModel, shapeCopy, resultModel, propResult);
        if (!node.checkExists())
        {
            return;
        }
        node.checkLangString(DCTerms.description, Occurrence.ExactlyOne,
            desc -> NodeCheck.checkSentence(desc));
        node.checkLiteral(OSLC.name, null, Occurrence.ExactlyOne,
            literal -> checkUnique(names,literal));
        node.checkURI(OSLC.occurs, Occurrence.ExactlyOne,
            uri -> Occurrence.isValidURI(uri) ? null : Terms.BadOccurs);
        node.checkURI(OSLC.propertyDefinition, Occurrence.ExactlyOne,
            uri -> {recordReference(propResult, uri); return checkUnique(predicates,uri); });
        node.checkURI(RDF.type, Occurrence.ExactlyOne,
            uri -> uri.equals(OSLC.Property.getURI()) ? null : Terms.WrongType);

        // Check the optional properties of the property definition
        node.checkLangString(DCTerms.title, Occurrence.ZeroOrOne, null);
        node.checkLiteral(OSLC.readOnly, XSDDatatype.XSDboolean, Occurrence.ZeroOrOne, null);
        node.checkLiteral(OSLC.hidden, XSDDatatype.XSDboolean, Occurrence.ZeroOrOne, null);
        node.checkLiteral(OSLC.isMemberProperty, XSDDatatype.XSDboolean, Occurrence.ZeroOrOne, null);
        node.checkURI(OSLC.range, Occurrence.ZeroOrMany, null);
        node.checkNode(OSLC.allowedValue, Occurrence.ZeroOrMany, null,
            uri -> {recordReference(propResult, uri); return null; });
        node.checkNode(OSLC.defaultValue, Occurrence.ZeroOrOne, null,
            uri -> {recordReference(propResult, uri); return null; });

        // TODO - add this shape to a list of shapes that must be defined and/or checked
        node.checkURI(OSLC.valueShape, Occurrence.ZeroOrOne, null);

        // Special check for value type
        checkValueType(propDef,propResult);

        // Special check for allowed values
        checkAllowedValues(propDef,propResult);

        // Allow and ignore optional properties for JRS
        node.checkLiteral(JRS.inversePropertyLabel, null, Occurrence.ZeroOrOne, null);
        node.checkLiteral(JRS.inContainer, XSDDatatype.XSDboolean, Occurrence.ZeroOrOne, null);
        node.checkURI(JRS.inversePropertyDefinition, Occurrence.ZeroOrOne, null);
        node.checkURI(JRS.superShape, Occurrence.ZeroOrMany, null);

        // Check that the prop def has no other properties
        StmtIterator it = shapeCopy.listStatements(propDef, null, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(propResult, Terms.Redundant, st.getPredicate());
            it.remove();
        }
    }


    /**
     * Add a dcterms:reference to the given check result;
     * this is used to record references to types and properties so
     * their occurrences in vocabularies can be cross-checked, and
     * unused terms reported.
     * @param resultNode the check result node in the ResultModel
     * @param uri the URI that has been referenced
     */
    private static void recordReference(final Resource resultNode, String uri)
    {
        resultNode.addProperty(DCTerms.references, ResourceFactory.createResource(uri));
    }


    /**
     * Check that a property has a value unique to the appropriate scope.
     * @param valueSet the scope set of the values to be checked
     * @param value the value of the property to be checked
     * @return the duplicate name error class if the name is not unique,
     * or null if the name is unique
     */
    @javax.annotation.CheckReturnValue
    @javax.annotation.Nullable
    public Resource checkUnique(Set<String> valueSet,String value)
    {
        if (valueSet.contains(value))
        {
            return Terms.Duplicate;
        }
        else
        {
            valueSet.add(value);
            return null;
        }
    }


    /**
     * Special checks for oslc:valueType property.
     *
     * @param propDef the property definition whose type is being checked
     * @param propResult the {@link ResultModel} resource to which to add errors
     */
    @javax.annotation.CheckReturnValue
    public void checkValueType(Resource propDef,Resource propResult)
    {
        Resource valueType = OSLC.Any;
        StmtIterator it = shapeModel.listStatements(propDef, OSLC.valueType, (RDFNode)null);
        if (it.hasNext())
        {
            RDFNode tnode;
            Statement st = it.next();
            shapeCopy.remove(st);
            if (!(tnode = st.getObject()).isResource())
            {
                resultModel.createIssue(propResult, Terms.NotResource, OSLC.valueType, tnode);
            }
            else
            {
                valueType = tnode.asResource();
            }
        }
        if (it.hasNext())
        {
            resultModel.createIssue(propResult, Terms.MoreThanOne, OSLC.valueType);
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
            resultModel.createIssue(propResult, Terms.LocalResourceDeprecated, OSLC.valueType);
            node.checkURI(OSLC.representation, Occurrence.ExactlyOne,
                uri -> {
                    Resource res = ResourceFactory.createResource(uri);
                    return res.equals(OSLC.Inline) ? null : Terms.MismatchingRepresentation; });
        }
        else if (valueType.equals(OSLC.AnyResource) || valueType.equals(OSLC.Resource))
        {
            node.checkURI(OSLC.representation, Occurrence.ExactlyOne,
                uri -> {
                    Resource res = ResourceFactory.createResource(uri);
                    return (finalValueType.equals(OSLC.AnyResource) ? (res.equals(OSLC.Inline) || res.equals(OSLC.Either))
                            : (res.equals(OSLC.Inline) || res.equals(OSLC.Either) || res.equals(OSLC.Reference)))
                                ? null : Terms.MismatchingRepresentation;
                });
        }
        else
        {
            resultModel.createIssue(propResult, Terms.WrongType, OSLC.valueType, valueType);
        }

        node.checkLiteral(OSLC.maxSize, XSDDatatype.XSDinteger, Occurrence.ZeroOrOne,
            lit -> finalValueType.equals(XSD.xstring) || finalValueType.equals(RDF.xmlLiteral) ? null : Terms.InappropriateMaxSize);
    }


    /**
     * Special checks for oslc:allowedValues property.
     *
     * @param propDef the property definition whose type is being checked
     * @param propResult the {@link ResultModel} resource to which to add errors
     */
    @javax.annotation.CheckReturnValue
    private void checkAllowedValues(Resource propDef, Resource propResult)
    {
        StmtIterator it = shapeModel.listStatements(propDef, OSLC.allowedValues, (RDFNode)null);
        while (it.hasNext())
        {
            RDFNode vnode;
            Statement st = it.next();
            shapeCopy.remove(st);
            if (!(vnode = st.getObject()).isResource())
            {
                resultModel.createIssue(propResult, Terms.NotResource, OSLC.allowedValues, vnode);
            }
            else
            {
                Resource allowedValues = vnode.asResource();
                NodeCheck node = new NodeCheck(allowedValues, httpHandler, shapeModel, shapeCopy, resultModel, propResult);
                if (node.checkExists())
                {
                    node.checkURI(RDF.type, Occurrence.ZeroOrOne,
                        uri -> uri.equals(OSLC.AllowedValues.getURI()) ? null : Terms.WrongType);
                    node.checkNode(OSLC.allowedValue, Occurrence.ZeroOrMany, null,
                        uri -> {recordReference(propResult, uri); return null; });
                }
            }
        }
    }
}
