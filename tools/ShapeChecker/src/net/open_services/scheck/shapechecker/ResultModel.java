package net.open_services.scheck.shapechecker;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Literal;
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
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import net.open_services.scheck.annotations.SCIssue;
import net.open_services.scheck.annotations.SCTerm;
import net.open_services.scheck.annotations.SCVocab;
import net.open_services.scheck.annotations.TermType;


/**
 * A Jena model used to store results of vocabulary and shape checking.
 *
 * <pre>
 * {@code
 * [
 *    # There is exactly one of these per run
 *    a sc:Summary ;
 *    dcterms:created dateTime ;
 *    sc:undefinedClass c1, c2, c3 ;
 *    sc:undefinedProp p1, p2, p3 ;
 *    sc:unusedVocab v1, v2, v3 ;
 *    sc:unusedTerm t1, t2, t3 ;
 *    sc:issueCount nnn
 * ]
 *
 * [
 *    # There is one of these for each vocabulary and shape URL provided
 *    a sc:VocabularyResult | sc:ShapesResult ;
 *    dcterms:source fileOrUrl ;
 *    sc:checks vocabUri ; # for sc:VocabularyResult only
 *    sc:issueCount nnn ;
 *    sc:result [
 *       # There is one of these for each ontology, vocabulary term, shape, and property definition
 *       a OntologyResult|TermResult|ShapeResult|PropertyResult ;
 *       sc:checks subjectUri ;
 *       dcterms:references uri ; # for each other resource mentioned by a property definition
 *       sc:issue [
 *          # There is one of these for each error or warning at the ontology, term, shape, or property level
 *          a SomeIssueClass ;
 *          sc:subject propertyUri ;
 *          sc:value badValue (uri or literal)
 *       ] , ...
 *    ]
 *    sc:issue [
 *       # There is one of these for each error or warning at the file level
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
//CSOFF: DeclarationOrderCheck
//CSOFF: ConstantNameCheck
//CSOFF: LineLengthCheck
@SCVocab(
    uri="http://open-services.net/ns/scheck#",
    prefix="scheck",
    domain="ShapeChecker Result Vocabulary.",
    description="A vocabulary for terms used in the result model of the OSLC Shape and Vocabulary checker.")
public class ResultModel
{
    private static final String checkerNS          = "http://open-services.net/ns/scheck#";


    private PrintStream                  printStream;
    private Model                        resultModel;
    private Model                        resultVocabModel;
    private Resource                     summaryResource;
    private Set<Resource>                suppressedIssues = new HashSet<>();
    private static Map<String, Resource> issueMap         = new HashMap<>();


    private static Resource resource(String local)
    {
        Resource resource = ResourceFactory.createResource(checkerNS + local);
        issueMap.put(local, resource);
        return resource;
    }

    private static Property property(String local)
    {
        return ResourceFactory.createProperty(checkerNS, local);
    }


    /** Summary result type. */
    @SCTerm(type=TermType.Class,description="A summary of the results of a check.")
    public static final Resource Summary          = resource("Summary");

    /** VocabResult outer result type. */
    @SCTerm(type=TermType.Class,description="A resource holding the check results for one vocabulary.")
    public static final Resource VocabResult      = resource("VocabResult");

    /** OntologyResult inner result type. */
    @SCTerm(type=TermType.Class,description="A resource holding the check results for the ontology resource for one vocabulary.")
    public static final Resource OntologyResult   = resource("OntologyResult");

    /** TermResult inner result type. */
    @SCTerm(type=TermType.Class,description="A resource holding the check results for one term of one vocabulary.")
    public static final Resource TermResult       = resource("TermResult");

    /** ShapesResult outer result type. */
    @SCTerm(type=TermType.Class,description="A resource holding the check results for the shapes from one source.")
    public static final Resource ShapesResult     = resource("ShapesResult");

    /** ShapeResult inner result type. */
    @SCTerm(type=TermType.Class,description="A resource holding the check results for a single shape.")
    public static final Resource ShapeResult      = resource("ShapeResult");

    /** PropertyResult inner result type. */
    @SCTerm(type=TermType.Class,description="A resource holding the check results for one property of one shape.")
    public static final Resource PropertyResult   = resource("PropertyResult");

    /** Error class for an oslc:impactType with an unknown value. */
    @SCIssue(description="Impact type must be one of oslc:UpstreamImpact, oslc:DownstreamImpact, oslc:SymmetricImpact, or oslc:NoImpact.")
    public static final Resource BadImpactType    = resource("BadImpactType");

    /** Error class for an oslc:occurs with an unknown value. */
    @SCIssue(description="The oslc:occurs property must be one of oslc:Exactly-one, oslc:One-or-many, oslc:Zero-or-one, or oslc:Zero-or-many.")
    public static final Resource BadOccurs        = resource("BadOccurs");

    /** Error class for a vs:term_status with an unknown value. */
    @SCIssue(description="The vs:term_status property must be either \\\"stable\\\" or \\\"archaic\\\"; \\\"unstable\\\" or \\\"testing\\\" are bad practice in published vocabularies.")
    public static final Resource BadTermStatus    = resource("BadTermStatus");

    /** Error class for an ill-formed XML literal. */
    @SCIssue(description="The XML literal is not well-formed.")
    public static final Resource BadXMLLiteral    = resource("BadXMLLiteral");

    /** Error class for a duplicate property value. */
    @SCIssue(description="This property name, definition, or oslc:describes is not unique.")
    public static final Resource Duplicate        = resource("Duplicate");

    /** Error class for a duplicate langauge-tagged string. */
    @SCIssue(description="Duplicate language on string literal.")
    public static final Resource DuplicateLangString = resource("DuplicateLangString");

    /** Error class for inappropriate use of the oslc:maxSize property. */
    @SCIssue(description="The property oslc:maxSize appplies only to string or XMLLiteral value types.")
    public static final Resource InappropriateMaxSize = resource("InappropriateMaxSize");

    /** Error class for unreadable or unparseable RDF. */
    @SCIssue(description="The target resource cannot be fetched or parsed as RDF.")
    public static final Resource InvalidRdf       = resource("InvalidRdf");

    /** Error class for an invalid URI as the object of some property. */
    @SCIssue(description="The URI of the target is invalid.")
    public static final Resource InvalidUri       = resource("InvalidUri");

    /** Error class for use of oslc:LocalResource - no longer recommended. */
    @SCIssue(description="Use of oslc:LocalResource should be replaced by oslc:AnyResource with oslc:representation=oslc:Inline.")
    public static final Resource LocalResourceDeprecated = resource("LocalResourceDeprecated");

    /** Error class for mismatching values of oslc:ValueType and oslc:Representation. */
    @SCIssue(description="Mismatching values of oslc:ValueType and oslc:Representation.")
    public static final Resource MismatchingRepresentation = resource("MismatchingRepresentation");

    /** Error class for a missing property of some node. */
    @SCIssue(description="The named property should be specified for this resource.")
    public static final Resource Missing          = resource("Missing");

    /** Error class for a missing period at the end of a description or comment. */
    @SCIssue(description="The description or comment should end with a period (full stop).")
    public static final Resource MissingPeriod    = resource("MissingPeriod");

    /** Error class for a property that occurs too many times. */
    @SCIssue(description="This property should appear at most once.")
    public static final Resource MoreThanOne      = resource("MoreThanOne");

    /** Error class for a term not defined by an ontology. */
    @SCIssue(description="This subject does not appear to be part of an ontology or one of its terms.")
    public static final Resource NoOntology       = resource("NoOntology");

    /** Error class for a property not defined by a shape. */
    @SCIssue(description="This property definition or other subject does not appear to be part of a defined shape.")
    public static final Resource NoShape          = resource("NoShape");

    /** Error class for a term or property that does not have a hash URI based on the parent URI. */
    @SCIssue(description="The term or property should use a hash URI, relative to its ontology or shape, respectively.")
    public static final Resource NotHash          = resource("NotHash");

    /** Error class for a resource property that should be a literal. */
    @SCIssue(description="A literal value is expected here, not a resource.")
    public static final Resource NotLiteral       = resource("NotLiteral");

    /** Error class for a literal property that should be a resource. */
    @SCIssue(description="A resource value is expected here, not a literal.")
    public static final Resource NotResource      = resource("NotResource");

    /** Error class for a redundant (unknown, unexpected) property of some node. */
    @SCIssue(description="This property is unexpected.")
    public static final Resource Redundant        = resource("Redundant");

    /** Error class for a source that does not refer to a Turtle URI. */
    @SCIssue(description="The vocabulary or shape source does not appear to be in Turtle.")
    public static final Resource SourceNotTurtle  = resource("SourceNotTurtle");

    /** Error class for a vocab term that is not defined by its parent ontology URI. */
    @SCIssue(description="This term does not have rdfs:isDefinedBy its parent ontology.")
    public static final Resource TermNotInVocab   = resource("TermNotInVocab");

    /** Error class for an undefined term. */
    @SCIssue(description="This resource is not defined in its parent vocabulary.")
    public static final Resource UndefinedTerm    = resource("UndefinedTerm");

    /** Error class for an unreachable resource. */
    @SCIssue(description="This resource is not fetchable.")
    public static final Resource Unreachable      = resource("Unreachable");

    /** Error class for a property of the wrong type. */
    @SCIssue(description="Wrong type for oslc:valueType, or for a property definition, or for a literal.")
    public static final Resource WrongType        = resource("WrongType");


    /** Predicate for a check. */
    @SCTerm(type=TermType.Property,description="The subject of a check.")
    public static final Property checks           = property("checks");

    /** Predicate for an issue. */
    @SCTerm(type=TermType.Property,description="An issue reported by the checker.")
    public static final Property issue            = property("issue");

    /** Predicate for an issue count. */
    @SCTerm(type=TermType.Property,description="The number of issues reported by the checker within the current scope.")
    public static final Property issueCount       = property("issueCount");

    /** Predicate for a result. */
    @SCTerm(type=TermType.Property,description="The check results for a set of resources within the current scope.")
    public static final Property result           = property("result");

    /** Predicate for the subject of an issue. */
    @SCTerm(type=TermType.Property,description="The subject of an issue.")
    public static final Property subject          = property("subject");

    /** Predicate for the value of an issue. */
    @SCTerm(type=TermType.Property,description="The value concerning which an issue is being reported.")
    public static final Property value            = property("value");


    /** Summary predicate for an undefined class. */
    @SCTerm(type=TermType.Property,description="These classes are referenced in the given shapes, but not defined in the expected vocabulary:")
    public static final Property undefinedClass   = property("undefinedClass");

    /** Summary predicate for an undefined term. */
    @SCTerm(type=TermType.Property,description="These properties or resources are referenced in the given shapes, but not defined in the expected vocabulary:")
    public static final Property undefinedProp    = property("undefinedProp");

    /** Summary predicate for an unreferenced vocabulary. */
    @SCTerm(type=TermType.Property,description="These vocabularies were given, but were not referenced in the given shapes:")
    public static final Property unusedVocabulary = property("unusedVocabulary");

    /** Summary predicate for an unreferenced vocabulary term. */
    @SCTerm(type=TermType.Property,description="These terms were defined in the given vocabularies, but were not referenced in the given shapes:")
    public static final Property unusedTerm       = property("unusedTerm");



    /**
     * Construct a new ResultModel.
     */
    public ResultModel()
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
            .addProperty(RDF.type, Summary)
            .addProperty(DCTerms.description, "ShapeCheck results")
            .addLiteral(DCTerms.created, currentDateTime);
    }


    /**
     * Get the result model.
     * @return the result model
     */
    public Model getModel()
    {
        return resultModel;
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
        parent.addProperty(result, inner);
        return inner;
    }


    /**
     * Suppress warnings about a named issue.
     * @param name the short name of the issue
     * @throws NoSuchElementException if the name is not that of a suppressible issue
     */
    public void suppressIssue(String name)
    {
        Resource r = issueMap.get(name);
        if (r == null)
        {
            throw new NoSuchElementException(name);
        }
        else
        {
            suppressedIssues.add(r);
        }
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
                    .addProperty(subject, subjectResource);
            if (object != null)
            {
                issueRes.addProperty(value, object);
            }
            resultNode.addProperty(issue, issueRes);
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
                    .addProperty(subject, literal);
            resultNode.addProperty(issue, issueRes);
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
    public Resource getSummary()
    {
        return summaryResource;
    }


    /**
     * Print a human-readable form of the result.
     * @param output the stream to which the result is written
     */
    public void print(PrintStream output)
    {
        printStream = output;
        resultVocabModel = ModelFactory.createDefaultModel().read("resources/SCVocabulary.ttl", "TURTLE");

        // Results from summary node
        output.printf("Results from ShapeChecker run on %s%n",
            ((XSDDateTime)summaryResource.getProperty(DCTerms.created).getLiteral().getValue()).asCalendar().getTime());
        int issues = summaryResource.getProperty(issueCount).getInt();
        printStream.printf("A total of %d issue%s found%n", issues, issues==1 ? " was" : "s were");

        // Results from vocabulary nodes
        ResIterator ri = resultModel.listResourcesWithProperty(RDF.type, VocabResult);
        while (ri.hasNext())
        {
            printVocabResults(ri.next());
        }

        // Results from shapes nodes
        ri = resultModel.listResourcesWithProperty(RDF.type, ShapesResult);
        while (ri.hasNext())
        {
            printShapesResults(ri.next());
        }

        // Finally, cross-check results
        printResList(undefinedClass);
        printResList(undefinedProp);
        printResList(unusedVocabulary);
        printResList(unusedTerm);
    }


    private void printResList(Property property)
    {
        StmtIterator sti = summaryResource.listProperties(property);
        if (sti.hasNext())
        {
            printStream.printf("%n%s%n",resultVocabModel.getProperty(property, RDFS.comment).getString());
            while (sti.hasNext())
            {
                Statement st = sti.next();
                printStream.printf("   %s%n",st.getResource().getURI());
            }
        }
    }


    private void printVocabResults(Resource vocab)
    {
        int issues = vocab.getProperty(issueCount).getInt();
        printStream.printf("%nChecked vocabulary %s from %s, with %d %s%n",
            vocab.getProperty(ResultModel.checks).getResource().getURI(),
            vocab.getProperty(DCTerms.source).getResource().getURI(),
            issues, issues==1 ? "issue" : "issues");

        printOuterIssues(vocab);
        printInnerIssues(vocab,"   ");
    }


    private void printShapesResults(Resource shapes)
    {
        int issues = shapes.getProperty(issueCount).getInt();
        printStream.printf("%nChecked shapes from %s, with %d %s%n",
            shapes.getProperty(DCTerms.source).getResource().getURI(),
            issues, issues==1 ? "issue" : "issues");

        printOuterIssues(shapes);
        printInnerIssues(shapes,"   ");
    }


    private void printOuterIssues(Resource outer)
    {
        StmtIterator sti = outer.listProperties(issue);
        if (sti.hasNext())
        {
            while (sti.hasNext())
            {
                Statement st = sti.next();
                printIssue("   ",st.getResource());
            }
        }
    }


    private void printInnerIssues(Resource inner,String prefix)
    {
        StmtIterator sti1 = inner.listProperties(result);
        while (sti1.hasNext())
        {
            Resource resultRes = sti1.next().getResource();
            Resource parent = resultRes.getPropertyResourceValue(ResultModel.checks);
            String parentName = parent == null ? "" : parent.getURI();
            if (resultRes.hasProperty(issue))
            {
                StmtIterator sti2 = resultRes.listProperties(issue);
                while (sti2.hasNext())
                {
                    Statement st = sti2.next();
                    printIssue(prefix+parentName+":",st.getResource());
                }
            }
            printInnerIssues(resultRes,prefix+parentName+":");
        }
    }


    private void printIssue(String prefix,Resource issueRes)
    {
        Resource subjectRes = issueRes.getPropertyResourceValue(subject);
        Resource type = issueRes.getPropertyResourceValue(RDF.type);
        Resource badValue = issueRes.getPropertyResourceValue(value);

        String badValStr = badValue == null ? ""
                : badValue.isResource() ? "=" + badValue.getURI()
                : "=" + badValue.asLiteral().getLexicalForm();

        printStream.printf("%s%s%s: %s%n",
            prefix,
            subjectRes.getURI(),
            badValStr,
            resultVocabModel.getProperty(type, RDFS.comment).getString());
    }
}
