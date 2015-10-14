package net.open_services.scheck.shapechecker;

import java.net.URI;

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
 * @author Nick Crossley. Released to public domain.
 */
public class VocabularyCheck
{
    private static final Property VS_TERM_STATUS = ResourceFactory.createProperty("http://www.w3.org/2003/06/sw-vocab-status/ns#term_status");

    private HttpHandler httpHandler;
    private Model       vocabModel;
    private Model       modelCopy;
    private ResultModel resultModel;
    private Resource    vocabResult;


    /**
     * Create a new vocabulary checker for the statements in a document
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
        vocabResult = this.resultModel.createOuterResult(ResultModel.VocabResult);
        vocabResult.addProperty(DCTerms.source,resultModel.getModel().createResource(document.toString()));
        vocabResult.addLiteral(DCTerms.extent, vocabModel.size());
    }


    /**
     * Perform a number of consistency checks on each vocabulary in the model.
     * @return the number of errors found
     */
    @javax.annotation.CheckReturnValue
    public int checkVocabularies()
    {
        int errors = 0;
        StmtIterator it = vocabModel.listStatements(null, RDF.type, OWL.Ontology);
        while (it.hasNext())
        {
            Statement st = it.next();
            modelCopy.remove(st);
            errors += checkVocabulary(st.getSubject());
        }

        StmtIterator sti = modelCopy.listStatements();
        while (sti.hasNext())
        {
            Statement st = sti.next();
            resultModel.createIssue(vocabResult, ResultModel.NoOntology, st.getSubject(), st.getPredicate());
            errors++;
            sti.remove();
        }
        vocabResult.addLiteral(ResultModel.issueCount, errors);
        return errors;
    }


    /**
     * Perform a number of consistency checks on a single vocabulary in the model.
     *
     * @param vocab the vocabulary resource to be checked
     * @return the number of errors found
     */
    @javax.annotation.CheckReturnValue
    public int checkVocabulary(Resource vocab)
    {
        int errors = 0;

        vocabResult.addProperty(ResultModel.checks, vocab);
        errors += checkOntologyProps(vocab);

        ResIterator ri = vocabModel.listResourcesWithProperty(RDFS.isDefinedBy);
        while (ri.hasNext())
        {
            Resource term = ri.next();
            errors += checkTerm(vocab, term);
        }

        return errors;
    }


    /**
     * Check the properties of the ontology resource for a vocabulary
     *
     * @param ontology the ontology resource to be checked
     * @return the number of errors found
     */
    @javax.annotation.CheckReturnValue
    private int checkOntologyProps(Resource ontology)
    {
        int errors = 0;
        Resource ontResult = resultModel.createInnerResult(vocabResult, ResultModel.OntologyResult);
        ontResult.addProperty(ResultModel.checks,ontology);

        // Check the mandatory properties of the ontology
        NodeCheck node = new NodeCheck(ontology, httpHandler, vocabModel, modelCopy, resultModel, ontResult);
        errors += node.checkLiteral(DCTerms.title, null, Occurrence.ExactlyOne, null);
        errors += node.checkLiteral(RDFS.label, null, Occurrence.ExactlyOne, null);
        errors += node.checkURI(DCTerms.source, Occurrence.ExactlyOne,
            (uri) -> (uri.matches(".*\\.ttl") ? null : ResultModel.SourceNotTurtle));

        // Check the optional properties of the ontology
        errors += node.checkNode(DCTerms.license, Occurrence.ZeroOrOne);
        errors += node.checkLiteral(DCTerms.description, null, Occurrence.ZeroOrOne, null);
        errors += node.checkLiteral(DCTerms.dateCopyrighted, null, Occurrence.ZeroOrOne, null);
        errors += node.checkLiteral(vocabModel.createProperty("http://purl.org/vocab/vann/preferredNamespacePrefix"),
            null, Occurrence.ZeroOrOne, null);

        errors += node.checkURI(RDFS.seeAlso, Occurrence.ZeroOrMany, null);

        StmtIterator it = modelCopy.listStatements(ontology, null, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(ontResult, ResultModel.Redundant, ontology, st.getPredicate());
            it.remove();
            errors++;
        }

        return errors;
    }


    /**
     * Check the properties of one term in a vocabulary
     *
     * @param vocab the vocabulary in which the term appears
     * @param term the term to be checked
     * @return the number of errors found
     */
    @javax.annotation.CheckReturnValue
    private int checkTerm(Resource vocab, Resource term)
    {
        int errors = 0;
        Resource termResult = resultModel.createInnerResult(vocabResult, ResultModel.TermResult);
        termResult.addProperty(ResultModel.checks,term);

        // Check the term is a hash resource with the same base as the
        // vocabulary itself
        if (term.isAnon() || term.getURI().matches(vocab.getURI() + "#[^/]+"))
        {
            errors++;
            resultModel.createIssue(termResult, ResultModel.NotHash, term);
        }

        // Check the mandatory properties of the term
        NodeCheck node = new NodeCheck(term, httpHandler, vocabModel, modelCopy, resultModel, termResult);
        errors += node.checkLiteral(RDFS.label, null, Occurrence.ExactlyOne, null);
        errors += node.checkLiteral(RDFS.comment, null, Occurrence.ExactlyOne, null);
        errors += node.checkURI(RDFS.isDefinedBy, Occurrence.ExactlyOne,
            (uri) -> (uri.equals(vocab.getURI()) ? null : ResultModel.TermNotInVocab));

        // Check the optional properties of the term
        errors += node.checkLiteral(OSLC.inverseLabel, null, Occurrence.ZeroOrOne, null);
        errors += node.checkLiteral(VS_TERM_STATUS, null, Occurrence.ZeroOrOne,
            (literal) -> (literal.matches("stable|archaic") ? null : ResultModel.BadTermStatus));
        errors += node.checkURI(RDFS.seeAlso, Occurrence.ZeroOrMany, null);
        errors += node.checkURI(OSLC.impactType, Occurrence.ZeroOrOne,
            (uri) -> (ImpactType.isValidURI(uri) ? null : ResultModel.BadImpactType));

        // Special checks for the term type, and the type-specific properties of a term
        errors += checkTermType(term, termResult);

        // Check that the term has no other properties
        StmtIterator it = modelCopy.listStatements(term, null, (RDFNode)null);
        while (it.hasNext())
        {
            Statement st = it.next();
            resultModel.createIssue(termResult, ResultModel.Redundant, st.getPredicate(), st.getObject());
            it.remove();
            errors++;
        }

        return errors;
    }


    /**
     * Special checks for rdf:type property
     *
     * @param term the term whose type is being checked
     * @param termResult the {@link ResultModel} resource to which to add errors
     * @return the number of errors found
     */
    @javax.annotation.CheckReturnValue
    public int checkTermType(Resource term, Resource termResult)
    {
        int errCount = 0;
        Resource termType = RDFS.Resource;
        StmtIterator it = vocabModel.listStatements(term, RDF.type, (RDFNode)null);
        if (it.hasNext())
        {
            RDFNode tnode;
            Statement st = it.next();
            modelCopy.remove(st);
            if (!(tnode = st.getObject()).isResource())
            {
                resultModel.createIssue(termResult, ResultModel.NotResource, RDF.type, tnode);
                errCount++;
                it.remove();
            }
            else
            {
                termType = tnode.asResource();
                it.remove();
            }
        }
        if (it.hasNext())
        {
            resultModel.createIssue(termResult, ResultModel.MoreThanOne, RDF.type);
            errCount++;
        }

        NodeCheck node = new NodeCheck(term, httpHandler, vocabModel, modelCopy, resultModel, termResult);
        if (termType.equals(RDFS.Class))
        {
            errCount += node.checkURI(RDFS.subClassOf, Occurrence.ZeroOrOne, null);
            errCount += node.checkURI(OWL.sameAs, Occurrence.ZeroOrMany, null);
        }
        else if (termType.equals(RDF.Property))
        {
            errCount += node.checkURI(RDFS.subPropertyOf, Occurrence.ZeroOrOne, null);
            errCount += node.checkURI(RDFS.range, Occurrence.ZeroOrOne, null);
            errCount += node.checkURI(RDFS.domain, Occurrence.ZeroOrOne, null);
            errCount += node.checkURI(OWL.sameAs, Occurrence.ZeroOrMany, null);
        }
        return errCount;
    }
}
