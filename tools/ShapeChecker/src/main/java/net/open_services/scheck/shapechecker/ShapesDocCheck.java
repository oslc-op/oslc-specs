package net.open_services.scheck.shapechecker;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


/**
 * Read and check the statements in each OSLC Shape in a document.
 * @author Nick Crossley. Released to public domain 2015.
 */
public class ShapesDocCheck
{
    private HttpHandler httpHandler;
    private URI         document;
    private Resource    docRes;
    private Model       shapeModel;
    private Model       shapeCopy;
    private ResultModel resultModel;
    private Resource    shapesResult;
    private Set<String> shapesWanted;
    private Set<String> shapesSeen;
    private Set<String> describes = new HashSet<>();
    private boolean     checkConstraints = false;


    /**
     * Create a new shape checker for the shapes in a document.
     *
     * @param document The document URI
     * @param shapesSeen A set of shape URIs that have been defined
     * @param shapesWanted A set of shape URIs that should be defined
     * @param httpHandler An HttpHandler to check for reachability of URI references
     * @param resultModel The model to which any results should be added
     * @param checkConstraints true if this shapes document must have a ResourceShapeConstraints resource
     */
    public ShapesDocCheck(URI document, Set<String> shapesSeen, Set<String> shapesWanted,
            HttpHandler httpHandler, ResultModel resultModel, boolean checkConstraints)
    {
        this.document = document;
        this.resultModel = resultModel;
        this.httpHandler = httpHandler;
        this.checkConstraints = checkConstraints;
        this.shapesSeen = shapesSeen;
        this.shapesWanted = shapesWanted;
        String docURI = document.toString();
        docRes = resultModel.getModel().createResource(docURI);
        shapeModel = ModelFactory.createDefaultModel().read(docURI, "TURTLE");
        shapeCopy = ModelFactory.createDefaultModel().add(shapeModel);
        shapesResult = resultModel.createOuterResult(Terms.ShapesResult);
        shapesResult.addProperty(Terms.checks,docRes);
        shapesResult.addProperty(DCTerms.source,docRes);
    }


    /**
     * Perform a number of consistency checks on each shape in the model.
     */
    public void checkShapes()
    {
        // First, check properties of this file or collection of shapes
        checkShapesDoc();

        StmtIterator it = shapeModel.listStatements(null, RDF.type, OSLC.ResourceShape);
        if (!it.hasNext())
        {
            resultModel.createIssue(shapesResult, Terms.NoShapesInFile, docRes);
            return;
        }

        while (it.hasNext())
        {
            Statement st = it.next();
            shapeCopy.remove(st);
            ShapeCheck shapeCheck = new ShapeCheck(
                shapesWanted,
                describes,
                httpHandler,
                shapeModel,
                shapeCopy,
                resultModel,
                shapesResult);
            Resource shapeResource = st.getSubject();
            shapeCheck.checkShape(document, shapeResource);
            shapesSeen.add(shapeResource.getURI());
        }

        // Give one error for each property definition not linked to a shape,
        // but then ignore all statements about that unused property definition
        List<Statement> sl = shapeCopy.listStatements(null, RDF.type, OSLC.Property).toList();
        for (Statement st : sl)
        {
            Resource subject = st.getSubject();
            resultModel.createIssue(shapesResult, Terms.NoShape, subject);
            it = shapeCopy.listStatements(subject, null, (RDFNode)null);
            while (it.hasNext())
            {
                it.next();
                it.remove();
            }
        }

        // Then give an error for each remaining statement in the model
        it = shapeCopy.listStatements();
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(shapesResult, Terms.NoShape, st.getSubject(), st.getPredicate());
            it.remove();
        }
    }


    private void checkShapesDoc()
    {
        Resource shapesUri;

        // Find one ResourceShapeConstraints resource
        if ((shapesUri = findResourceShapeConstraints()) != null)
        {
            // Check mandatory properties of the shapes collection
            NodeCheck node = new NodeCheck(shapesUri, httpHandler, shapeModel, shapeCopy, resultModel, shapesResult);
            node.checkLiteral(DCTerms.title, null, Occurrence.ExactlyOne, null);
            node.checkNode(DCTerms.license, Occurrence.ExactlyOne);
            node.checkLiteral(DCTerms.hasVersion, null, Occurrence.ExactlyOne, null);
            node.checkSuppressibleURI(DCTerms.source, Occurrence.ExactlyOne, false, false,
                uri -> uri.matches(".*\\.ttl") ? null : Terms.SourceNotTurtle);
            node.checkSuppressibleURI(DCTerms.isPartOf, Occurrence.ExactlyOne, false, false, null);

            // Check optional properties of the shapes collection
            node.checkLiteral(DCTerms.description, null, Occurrence.ZeroOrOne,
                desc -> NodeCheck.checkSentence(desc));
            node.checkLiteral(DCTerms.modified, null, Occurrence.ZeroOrOne, null);
            node.checkSuppressibleURI(RDFS.seeAlso, Occurrence.ZeroOrMany, false, false, null);
            node.checkSuppressibleURI(DCTerms.publisher, Occurrence.ZeroOrMany,false, false, null);
            node.checkLiteral(RDFS.label, null, Occurrence.ZeroOrOne, null);
            node.checkLiteral(DCTerms.issued, null, Occurrence.ZeroOrOne, null);
            node.checkLiteral(DCTerms.dateCopyrighted, null, Occurrence.ZeroOrOne, null);

            // Check that the shape collection has no other properties
            StmtIterator it = shapeCopy.listStatements(shapesUri, null, (RDFNode)null);
            while (it.hasNext())
            {
                Statement st = it.next();
                resultModel.createIssue(shapesResult, Terms.Redundant, st.getPredicate());
                it.remove();
            }

        }
    }


    private Resource findResourceShapeConstraints()
    {
        StmtIterator it = shapeModel.listStatements(null, RDF.type, OSLC.ResourceShapeConstraints);

        if (!it.hasNext())
        {
            if (checkConstraints)
            {
                resultModel.createIssue(shapesResult, Terms.NoResourceShapeConstraints, docRes);
            }
            return null;
        }

        Statement st = it.next();
        shapeCopy.remove(st);

        if (it.hasNext())
        {
            resultModel.createIssue(shapesResult, Terms.MultipleResourceShapeConstraints, docRes);
        }
        return st.getSubject();
    }
}
