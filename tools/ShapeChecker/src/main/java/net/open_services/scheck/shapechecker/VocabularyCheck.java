package net.open_services.scheck.shapechecker;

import java.net.URI;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;



/**
 * Read and check the statements in an RDF vocabulary (owl:Ontology).
 * @author Nick Crossley. Released to public domain 2015.
 */
public class VocabularyCheck
{
    private static final Property VS_TERM_STATUS = ResourceFactory.createProperty("http://www.w3.org/2003/06/sw-vocab-status/ns#term_status");

    private HttpHandler httpHandler;
    private Model       vocabModel;
    private Model       modelCopy;
    private ResultModel resultModel;
    private Resource    vocabResult;
    private Property    preferredNameSpace;


    /**
     * Create a new vocabulary checker for the statements in a document.
     *
     * @param document The document URI
     * @param httpHandler An HttpHandler to check for reachability of URI references
     * @param resultModel The {@link ResultModel} to which any results should be added
     */
    public VocabularyCheck(URI document, HttpHandler httpHandler, ResultModel resultModel)
    {
        vocabModel = ModelFactory.createDefaultModel().read(document.toString(), "TURTLE");
        modelCopy = ModelFactory.createDefaultModel().add(vocabModel);
        this.httpHandler = httpHandler;
        this.resultModel = resultModel;
        vocabResult = this.resultModel.createOuterResult(Terms.VocabResult);
        vocabResult.addProperty(DCTerms.source,resultModel.getModel().createResource(document.toString()));
        vocabResult.addLiteral(DCTerms.extent, vocabModel.size());
        preferredNameSpace = vocabModel.createProperty("http://purl.org/vocab/vann/preferredNamespacePrefix");
    }


    /**
     * Perform a number of consistency checks on each vocabulary in the model.
     */
    public void checkVocabularies()
    {
        StmtIterator it = vocabModel.listStatements(null, RDF.type, OWL.Ontology);
        while (it.hasNext())
        {
            Statement st = it.next();
            modelCopy.remove(st);
            checkVocabulary(st.getSubject());
        }

        StmtIterator sti = modelCopy.listStatements();
        while (sti.hasNext())
        {
            Statement st = sti.next();
            resultModel.createIssue(vocabResult, Terms.NoOntology, st.getSubject(), st.getPredicate());
            sti.remove();
        }
    }


    /**
     * Perform a number of consistency checks on a single vocabulary in the model.
     *
     * @param vocab the vocabulary resource to be checked
     */
    @javax.annotation.CheckReturnValue
    public void checkVocabulary(Resource vocab)
    {
        vocabResult.addProperty(Terms.checks, vocab);
        checkOntologyProps(vocab);

        ResIterator ri = vocabModel.listResourcesWithProperty(RDFS.isDefinedBy);
        while (ri.hasNext())
        {
            Resource term = ri.next();
            checkTerm(vocab, term);
        }
    }


    /**
     * Check the properties of the ontology resource for a vocabulary.
     *
     * @param ontology the ontology resource to be checked
     */
    @javax.annotation.CheckReturnValue
    private void checkOntologyProps(Resource ontology)
    {
        Resource ontResult = resultModel.createInnerResult(vocabResult, Terms.OntologyResult);
        ontResult.addProperty(Terms.checks,ontology);

        // Check the mandatory properties of the ontology
        NodeCheck node = new NodeCheck(ontology, httpHandler, vocabModel, modelCopy, resultModel, ontResult);
        node.checkLiteral(DCTerms.title, null, Occurrence.ExactlyOne, null);
        node.checkLiteral(RDFS.label, null, Occurrence.ExactlyOne, null);
        node.checkURI(DCTerms.source, Occurrence.ExactlyOne,
            (uri) -> (uri.matches(".*\\.ttl") ? null : Terms.SourceNotTurtle));

        // Check the optional properties of the ontology
        node.checkNode(DCTerms.license, Occurrence.ZeroOrOne);
        node.checkLiteral(DCTerms.description, null, Occurrence.ZeroOrOne,
            (desc) -> (NodeCheck.checkPeriod(desc)));
        node.checkLiteral(DCTerms.dateCopyrighted, null, Occurrence.ZeroOrOne, null);
        node.checkLiteral(preferredNameSpace, null, Occurrence.ZeroOrOne, null);

        node.checkURI(RDFS.seeAlso, Occurrence.ZeroOrMany, null);

        StmtIterator it = modelCopy.listStatements(ontology, null, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(ontResult, Terms.Redundant, ontology, st.getPredicate());
            it.remove();
        }
    }


    /**
     * Check the properties of one term in a vocabulary.
     *
     * @param vocab the vocabulary in which the term appears
     * @param term the term to be checked
     */
    @javax.annotation.CheckReturnValue
    private void checkTerm(Resource vocab, Resource term)
    {
        Resource termResult = resultModel.createInnerResult(vocabResult, Terms.TermResult);
        termResult.addProperty(Terms.checks,term);

        // Check the term is a hash resource with the same base as the vocabulary itself
        if (term.isAnon() || term.getURI().matches(vocab.getURI() + "#[^/]+"))
        {
            resultModel.createIssue(termResult, Terms.NotHash, term);
        }

        // Check the mandatory properties of the term
        NodeCheck node = new NodeCheck(term, httpHandler, vocabModel, modelCopy, resultModel, termResult);
        node.checkLiteral(RDFS.label, null, Occurrence.ExactlyOne, null);
        node.checkLiteral(RDFS.comment, null, Occurrence.ExactlyOne,
            (comment) -> (NodeCheck.checkPeriod(comment)));
        node.checkURI(RDFS.isDefinedBy, Occurrence.ExactlyOne,
            (uri) -> (uri.equals(vocab.getURI()) ? null : Terms.TermNotInVocab));

        // Check the optional properties of the term
        node.checkLiteral(OSLC.inverseLabel, null, Occurrence.ZeroOrOne, null);
        node.checkLiteral(OSLC.hidden, XSDDatatype.XSDboolean, Occurrence.ZeroOrOne, null);
        node.checkLiteral(VS_TERM_STATUS, null, Occurrence.ZeroOrOne,
            (literal) -> (literal.matches("stable|archaic") ? null : Terms.BadTermStatus));
        node.checkURI(RDFS.seeAlso, Occurrence.ZeroOrMany, null);
        node.checkURI(OSLC.impactType, Occurrence.ZeroOrOne,
            (uri) -> (ImpactType.isValidURI(uri) ? null : Terms.BadImpactType));

        // Special checks for the term type, and the type-specific properties of a term
        checkTermType(term, termResult);

        // Check that the term has no other properties
        StmtIterator it = modelCopy.listStatements(term, null, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(termResult, Terms.Redundant, st.getPredicate(), st.getObject());
            it.remove();
        }
    }


    /**
     * Special checks for rdf:type property.
     *
     * @param term the term whose type is being checked
     * @param termResult the {@link Terms} resource to which to add errors
     */
    @javax.annotation.CheckReturnValue
    public void checkTermType(Resource term, Resource termResult)
    {
        Resource termType = RDFS.Resource;
        StmtIterator it = vocabModel.listStatements(term, RDF.type, (RDFNode)null);
        if (it.hasNext())
        {
            RDFNode tnode;
            Statement st = it.next();
            modelCopy.remove(st);
            if (!(tnode = st.getObject()).isResource())
            {
                resultModel.createIssue(termResult, Terms.NotResource, RDF.type, tnode);
            }
            else
            {
                termType = tnode.asResource();
            }
        }
        else
        {
            resultModel.createIssue(termResult, Terms.Missing, RDF.type);
        }
        if (it.hasNext())
        {
            resultModel.createIssue(termResult, Terms.MoreThanOne, RDF.type);
        }

        NodeCheck node = new NodeCheck(term, httpHandler, vocabModel, modelCopy, resultModel, termResult);
        if (termType.equals(RDFS.Class))
        {
            node.checkURI(RDFS.subClassOf, Occurrence.ZeroOrOne, null);
            node.checkURI(OWL.sameAs, Occurrence.ZeroOrMany, null);
        }
        else if (termType.equals(RDF.Property))
        {
            node.checkURI(RDFS.subPropertyOf, Occurrence.ZeroOrOne, null);
            node.checkURI(RDFS.range, Occurrence.ZeroOrOne, null);
            node.checkURI(RDFS.domain, Occurrence.ZeroOrOne, null);
            node.checkURI(OWL.sameAs, Occurrence.ZeroOrMany, null);
        }
    }
}
