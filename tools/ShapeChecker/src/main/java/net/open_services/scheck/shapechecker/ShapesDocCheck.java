package net.open_services.scheck.shapechecker;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;


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
    private Set<String> describes = new HashSet<>();


    /**
     * Create a new shape checker for the shapes in a document.
     *
     * @param document The document URI
     * @param httpHandler An HttpHandler to check for reachability of URI references
     * @param resultModel The model to which any results should be added
     */
    public ShapesDocCheck(URI document, HttpHandler httpHandler, ResultModel resultModel)
    {
        this.document = document;
        this.resultModel = resultModel;
        this.httpHandler = httpHandler;
        String docURI = document.toString();
        docRes = resultModel.getModel().createResource(docURI);
        shapeModel = ModelFactory.createDefaultModel().read(docURI, "TURTLE");
        shapeCopy = ModelFactory.createDefaultModel().add(shapeModel);
        shapesResult = resultModel.createOuterResult(Terms.ShapesResult);
        shapesResult.addProperty(DCTerms.source,docRes);
        shapesResult.addLiteral(DCTerms.extent, shapeModel.size());
    }


    /**
     * Perform a number of consistency checks on each shape in the model.
     */
    public void checkShapes()
    {
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
            ShapeCheck shapeCheck = new ShapeCheck(describes, httpHandler, shapeModel, shapeCopy, resultModel, shapesResult);
            shapeCheck.checkShape(document, st.getSubject());
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
}
