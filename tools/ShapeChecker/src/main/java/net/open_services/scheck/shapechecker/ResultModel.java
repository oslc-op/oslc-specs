package net.open_services.scheck.shapechecker;

import java.util.*;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;


/**
 * A Jena model used to store results of vocabulary and shape checking.
 *
 * <pre>
 * {@code
 * [
 *    # There is exactly one of these per run
 *    a sc:Summary ;
 *    dcterms:created dateTime ;
 *    dcterms:description "string including the command line arguments" ;
 *    sc:undefinedClass c1, c2, c3, ... ;
 *    sc:undefinedProp p1, p2, p3, ... ;
 *    sc:unusedVocab v1, v2, v3, ... ;
 *    sc:unusedTerm t1, t2, t3, ... ;
 *
 *    sc:issueCount [
 *       # There is one of these per run, added by post-processing;
 *       # this includes the counts from all vocabularies and shapes.
 *       sc:infoCount nnn ; sc:warnCount nnn ; sc:errorCount nnn
 *    ]
 * ]
 *
 * [
 *    # There is one of these for each vocabulary and shape URL provided
 *    a sc:VocabularyResult | sc:ShapesResult ;
 *    dcterms:source fileOrUrl ;
 *    dcterms:title "title or similar name of vocab or shapes resource" ??? TODO DO I WANT THIS?
 *    sc:checks Uri ; #resultModel always the same as dcterms:source - TODO so why have both?
 *    sc:issueCount [
 *       # The total numbers of issues reported against this vocabulary or shape, added by post-processing.
 *       sc:infoCount nnn ; sc:warnCount nnn ; sc:errorCount nnn
 *    ]
 *    sc:result [
 *       # There is one of these for each ontology, vocabulary term, shape
 *       a OntologyResult|TermResult|ShapeResult|PropertyResult ;
 *       sc:checks subjectUri ; #resultModel the URI of the thing being checked (ontology, vocabulary term, shape)
 *       dcterms:references uri ; # for each other resource mentioned by a property definition
 *       sc:result [
 *          # There is a nested sc:result for each property definition in a shape
 *       ] , ...
 *       sc:issue [
 *          # There is one of these 'inner issues' for each issue at the ontology, term, shape, or property level
 *          a SomeIssueClass ;
 *          sc:subject propertyUri ;
 *          sc:value badValue (uri or literal)
 *       ] , ...
 *    ] , ...
 *    sc:issue [
 *       # There is one of these 'outer issues' for each issue at the vocabulary or shape file level
 *       a SomeIssueClass ;
 *       sc:subject propertyUri ;
 *       sc:value badValue (uri or literal)
 *    ] , ...
 * ]
 * }
 * </pre>
 *
 * @author Nick Crossley. Released to public domain 2015.
 */
public class ResultModel
{
    private Model         resultModel;
    private Model         resultVocabModel;
    private Resource      summaryResource;
    private Set<Resource> suppressedIssues = new HashSet<>();


    /**
     * Construct a new ResultModel.
     * @param cmdArgs the command line arguments
     */
    public ResultModel(String... cmdArgs)
    {
        resultModel = ModelFactory.createDefaultModel();
        resultModel.setNsPrefix("dcterms",     "http://purl.org/dc/terms/");
        resultModel.setNsPrefix("oslc",        "http://open-services.net/ns/core#");
        resultModel.setNsPrefix("oslc_am",     "http://open-services.net/ns/am#");
        resultModel.setNsPrefix("oslc_check",  "http://open-services.net/ns/scheck#");
        resultModel.setNsPrefix("oslc_cm",     "http://open-services.net/ns/cm#");
        resultModel.setNsPrefix("oslc_config", "http://open-services.net/ns/config#");
        resultModel.setNsPrefix("oslc_qm",     "http://open-services.net/ns/qm#");
        resultModel.setNsPrefix("oslc_rm",     "http://open-services.net/ns/rm#");
        resultModel.setNsPrefix("owl",         "http://www.w3.org/2002/07/owl#");
        resultModel.setNsPrefix("rdf",         "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        resultModel.setNsPrefix("xsd",         "http://www.w3.org/2001/XMLSchema#");

        Calendar cal = Calendar.getInstance();
        Literal currentDateTime = resultModel.createTypedLiteral(cal);
        summaryResource = resultModel.createResource()
            .addProperty(RDF.type, Terms.Summary)
            .addProperty(DCTerms.description, String.join(" ",cmdArgs))
            .addLiteral(DCTerms.created, currentDateTime);
    }


    /**
     * Get the result model.
     * @return the result model
     */
    @javax.annotation.CheckReturnValue
    public Model getModel()
    {
        return resultModel;
    }


    /**
     * Get the vocabulary model.
     * @return the vocabulary model
     */
    @javax.annotation.CheckReturnValue
    public Model getResultVocabModel()
    {
        return resultVocabModel;
    }


    /**
     * Create an anonymous outer node to hold results for a vocabulary or shape.
     * @param type the type of the outer node ({@code VocabResult} or {@code ShapesResult})
     * @return the resource created
     */
    @javax.annotation.CheckReturnValue
    public Resource createOuterResult(Resource type)
    {
        return resultModel.createResource().addProperty(RDF.type, type);
    }


    /**
     * Create an anonymous inner node to hold results for parts of vocabularies or shapes.
     * @param parent the parent node
     * @param type the type of the inner node
     * (@code OntologyResult}, {@code TermResult}, {@code ShapeResult}, or {@code PropertyResult})
     * @return the resource created
     */
    @javax.annotation.CheckReturnValue
    public Resource createInnerResult(Resource parent, Resource type)
    {
        Resource inner = resultModel.createResource().addProperty(RDF.type, type);
        parent.addProperty(Terms.result, inner);
        return inner;
    }


    /**
     * Suppress warnings about a named issue.
     * Silently ignores requests to suppress unknown issues.
     * @param name the short name of the issue
     */
    public void suppressIssue(String name)
    {
        Terms.findIssue(name).ifPresent(r -> suppressedIssues.add(r));
    }


    /**
     * Create an anonymous node to hold info about an issue.
     * @param resultNode the parent of the new anonymous node
     * @param type the type of the issue node
     * @param subjectResource the subject of the issue
     * @param object the value with which there is an issue (may be null)
     */
    public void createIssue(Resource resultNode, Resource type, Resource subjectResource, RDFNode object)
    {
        assert type != null;
        assert subjectResource != null;

        if (!suppressedIssues.contains(type))
        {
            Resource issueRes = resultModel.createResource()
                    .addProperty(RDF.type, type)
                    .addProperty(Terms.subject, subjectResource);
            if (object != null)
            {
                issueRes.addProperty(Terms.value, object);
            }
            resultNode.addProperty(Terms.issue, issueRes);
        }
    }


    /**
     * Create an anonymous node to hold info about an issue.
     * @param resultNode the parent of the new anonymous node
     * @param type the type of the issue node
     * @param literal the subject of the issue
     */
    public void createIssue(Resource resultNode, Resource type, String literal)
    {
        assert type != null;
        assert literal != null;

        if (!suppressedIssues.contains(type))
        {
            Resource issueRes = resultModel.createResource()
                    .addProperty(RDF.type, type)
                    .addProperty(Terms.subject, literal);
            resultNode.addProperty(Terms.issue, issueRes);
        }
    }


    /**
     * Create an anonymous node to hold info about an issue.
     * @param resultNode the parent of the new anonymous node
     * @param type the type of the issue node
     * @param subjectResource the subject of the issue
     */
    public void createIssue(Resource resultNode, Resource type, Resource subjectResource)
    {
        createIssue(resultNode, type, subjectResource, null);
    }


    /**
     * Create an anonymous node to hold info about an issue.
     * @param resultNode the parent of the new anonymous node
     * @param e info about the issue
     */
    public void createIssue(Resource resultNode, ShapeCheckException e)
    {
        createIssue(resultNode, e.getIssueClass(), e.getSubject(), e.getObject());
    }


    /**
     * Get the summary resource.
     * @return the summary resource
     */
    @javax.annotation.CheckReturnValue
    public Resource getSummary()
    {
        return summaryResource;
    }


    /**
     * Add a summary issue with a resource as the object.
     * @param type the type of the issue
     * @param resource the resource (vocabulary, shape, term) for the issue
     */
    public void addSummaryIssue(Property type, Resource resource)
    {
        if (!suppressedIssues.contains(type))
        {
            getSummary().addProperty(type, resource);
        }
    }


    /**
     * Add a summary issue with a literal as the object.
     * @param type the type of the issue
     * @param object the string literal for the issue
     */
    public void addSummaryIssue(Property type, String object)
    {
        if (!suppressedIssues.contains(type))
        {
            getSummary().addProperty(type, object);
        }
    }


    /**
     * Add counts of each issue severity.
     * @param debug the current debug level
     * @return the number of errors found.
     */
    @javax.annotation.CheckReturnValue
    public int summarizeIssues(int debug)
    {
        resultVocabModel = ModelFactory.createDefaultModel()
                .read(ResultModel.class.getClassLoader().getResourceAsStream("SCVocabulary.ttl"),
                    "http://open-services.net/ns/scheck#",
                    "TURTLE");
        if (debug > 3)
        {
            System.err.println("\nVocabulary model:");
            Models.write(resultVocabModel, System.err);
        }
        return new IssueSummarizer(this).countIssues();
    }
}
