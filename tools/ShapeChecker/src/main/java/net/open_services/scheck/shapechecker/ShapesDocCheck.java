package net.open_services.scheck.shapechecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    private void checkShapesDoc()
    {
    	Resource shapesUri;

    	// Read shapes file as text, scanning for @base statement (if any)
    	if ((shapesUri = findBase()) != null)
    	{
	        // Check optional properties of the shapes file or collection
	        NodeCheck node = new NodeCheck(shapesUri, httpHandler, shapeModel, shapeCopy, resultModel, shapesResult);
	        node.checkLiteral(DCTerms.title, null, Occurrence.ZeroOrOne, null);
	        node.checkLiteral(RDFS.label, null, Occurrence.ZeroOrOne, null);
	        node.checkLiteral(DCTerms.description, null, Occurrence.ZeroOrOne,
	            desc -> NodeCheck.checkSentence(desc));
	        node.checkSuppressibleURI(DCTerms.source, Occurrence.ZeroOrOne, false, false,
	            uri -> uri.matches(".*\\.ttl") ? null : Terms.SourceNotTurtle);
	        node.checkNode(DCTerms.license, Occurrence.ZeroOrOne);
	        node.checkLiteral(DCTerms.issued, null, Occurrence.ZeroOrOne, null);
	        node.checkLiteral(DCTerms.dateCopyrighted, null, Occurrence.ZeroOrOne, null);
	        node.checkLiteral(DCTerms.modified, null, Occurrence.ZeroOrOne, null);
	        node.checkSuppressibleURI(RDFS.seeAlso, Occurrence.ZeroOrMany, false, false, null);
	        node.checkSuppressibleURI(DCTerms.publisher, Occurrence.ZeroOrMany,false, false, null);
	        node.checkLiteral(DCTerms.hasVersion, null, Occurrence.ZeroOrOne, null);

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


	private Resource findBase()
	{
		Pattern p = Pattern.compile(".*@base\\s+<(.*)>\\s+\\.");
		try (BufferedReader in = new BufferedReader(new InputStreamReader(document.toURL().openStream())))
		{
			String line;
			while ((line = in.readLine()) != null)
			{
				Matcher matcher = p.matcher(line);
				if (matcher.matches())
				{
					return shapeModel.createResource(matcher.group(1));
				}
			}
		}
		catch (IOException e)
		{
			// fall through
		}
		resultModel.createIssue(shapesResult, Terms.NoBaseURI, docRes);
		return null;
	}
}
