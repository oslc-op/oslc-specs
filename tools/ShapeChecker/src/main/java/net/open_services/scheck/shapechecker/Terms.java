package net.open_services.scheck.shapechecker;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import net.open_services.scheck.annotations.*;

import static net.open_services.scheck.annotations.IssueSeverity.*;

//CSOFF: DeclarationOrderCheck
//CSOFF: ConstantNameCheck
//CSOFF: LineLengthCheck


/**
 * A set of vocabulary terms for use in the ResultModel.
 *
 * @author Nick Crossley. Released to public domain 2019.
 */
@SCVocab(
    uri="http://open-services.net/ns/scheck#",
    prefix="scheck",
    domain="ShapeChecker Result Vocabulary.",
    description="A vocabulary for terms used in the result model of the OSLC Shape and Vocabulary checker.")
public final class Terms
{
    private static final String checkerNS          = "http://open-services.net/ns/scheck#";

    private static Map<String, Resource> issueMap  = new HashMap<>();


    /**
     * No instantiation.
     */
    private Terms() {}


    /**
     * Find an issue by name.
     * @param name the name of an issue
     * @return the issue found, if it exists
     */
    public static Optional<Resource> findIssue(String name)
    {
        return Optional.ofNullable(Terms.issueMap.get(name));
    }


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
    @SCTerm(type=TermType.Class,description="The summary of the results of a check.")
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

    /** Error class for a term with the wrong case. */
    @SCIssue(issueSeverity=Warning,description="A vocabulary class must start with upper case, and a property with lower case.")
    public static final Resource BadCase          = resource("BadCase");

    /** Error class for an oslc:impactType with an unknown value. */
    @SCIssue(issueSeverity=Error,description="Impact type must be one of oslc:UpstreamImpact, oslc:DownstreamImpact, oslc:SymmetricImpact, or oslc:NoImpact.")
    public static final Resource BadImpactType    = resource("BadImpactType");

    /** Error class for an oslc:occurs with an unknown value. */
    @SCIssue(issueSeverity=Error,description="The oslc:occurs property must be one of oslc:Exactly-one, oslc:One-or-many, oslc:Zero-or-one, or oslc:Zero-or-many.")
    public static final Resource BadOccurs        = resource("BadOccurs");

    /** Error class for a vs:term_status with an unknown value. */
    @SCIssue(issueSeverity=Error,description="The vs:term_status property must be either \\\"stable\\\" or \\\"archaic\\\"; \\\"unstable\\\" or \\\"testing\\\" are bad practice in published vocabularies.")
    public static final Resource BadTermStatus    = resource("BadTermStatus");

    /** Error class for an unknown vocabulary term type. */
    @SCIssue(issueSeverity=Error,description="A vocabulary term must be of type rdfs:Class, rdf:Property, rdfs:Resource, or some other valid URI.")
    public static final Resource BadTermType    = resource("BadTermType");

    /** Error class for an ill-formed XML literal. */
    @SCIssue(issueSeverity=Warning,description="The XML literal is not well-formed.")
    public static final Resource BadXMLLiteral    = resource("BadXMLLiteral");

    /** Error class for a duplicate property value. */
    @SCIssue(issueSeverity=Error,description="This property name, definition, or oslc:describes is not unique.")
    public static final Resource Duplicate        = resource("Duplicate");

    /** Error class for a duplicate property value. */
    @SCIssue(issueSeverity=Warning,description="This label is not unique.")
    public static final Resource DuplicateLabel   = resource("DuplicateLabel");

    /** Error class for a duplicate language-tagged string. */
    @SCIssue(issueSeverity=Error,description="Duplicate language on string literal.")
    public static final Resource DuplicateLangString = resource("DuplicateLangString");

    /** Error class for embedded whitespace after newline in comment or description. */
    @SCIssue(issueSeverity=Warning,description="White space after embedded newline - misuse of triple quotes?")
    public static final Resource EmbeddedWhitespace = resource("EmbeddedWhitespace");

   /** Error class for inappropriate use of the oslc:maxSize property. */
    @SCIssue(issueSeverity=Error,description="The property oslc:maxSize appplies only to string or XMLLiteral value types.")
    public static final Resource InappropriateMaxSize = resource("InappropriateMaxSize");

    /** Warning class for unreadable or unparseable RDF. */
    @SCIssue(issueSeverity=Warning,description="The target resource cannot be fetched or parsed as RDF.")
    public static final Resource InvalidRdfWarn       = resource("InvalidRdfWarn");

    /** Error class for unreadable or unparseable RDF. */
    @SCIssue(issueSeverity=Error,description="The target resource cannot be fetched or parsed as RDF.")
    public static final Resource InvalidRdfError      = resource("InvalidRdfError");

    /** Error class for an invalid URI as the object of some property. */
    @SCIssue(issueSeverity=Error,description="The URI of the target is invalid.")
    public static final Resource InvalidUri           = resource("InvalidUri");

    /** Error class for use of oslc:LocalResource - no longer recommended. */
    @SCIssue(issueSeverity=Warning,description="Use of oslc:LocalResource should be replaced by oslc:AnyResource with oslc:representation=oslc:Inline.")
    public static final Resource LocalResourceDeprecated = resource("LocalResourceDeprecated");

    /** Error class for mismatching values of oslc:ValueType and oslc:Representation. */
    @SCIssue(issueSeverity=Error,description="Mismatching values of oslc:ValueType and oslc:Representation.")
    public static final Resource MismatchingRepresentation = resource("MismatchingRepresentation");

    /** Warning class for a missing property of some node. */
    @SCIssue(issueSeverity=Warning,description="The named property should be specified for this resource.")
    public static final Resource MissingWarn          = resource("MissingWarn");

    /** Error class for a missing property of some node. */
    @SCIssue(issueSeverity=Error,description="The named property should be specified for this resource.")
    public static final Resource MissingError          = resource("MissingError");

    /** Error class for a missing period at the end of a description or comment. */
    @SCIssue(issueSeverity=Warning,description="The description or comment should end with a period (full stop).")
    public static final Resource MissingPeriod    = resource("MissingPeriod");

    /** Error class for a property that occurs too many times. */
    @SCIssue(issueSeverity=Error,description="This property should appear at most once.")
    public static final Resource MoreThanOne      = resource("MoreThanOne");

    /** Error class for a term not defined by an ontology. */
    @SCIssue(issueSeverity=Warning,description="This subject does not appear to be part of an ontology or one of its terms.")
    public static final Resource NoOntology       = resource("NoOntology");

    /** Error class for a property not defined by a shape. */
    @SCIssue(issueSeverity=Warning,description="This property definition or other subject does not appear to be part of a defined shape.")
    public static final Resource NoShape          = resource("NoShape");

    /** Error class for an resource not found. */
    @SCIssue(issueSeverity=Warning,description="This resource is not defined in this document.")
    public static final Resource NotDefined       = resource("NotDefined");

    /** Error class for a term or property that does not have a hash URI based on the parent URI. */
    @SCIssue(issueSeverity=Info,description="The term or property should use a hash URI, relative to its ontology or shape, respectively.")
    public static final Resource NotHash          = resource("NotHash");

    /** Error class for a resource property that should be a literal. */
    @SCIssue(issueSeverity=Error,description="A literal value is expected here, not a resource.")
    public static final Resource NotLiteral       = resource("NotLiteral");

    /** Error class for a literal property that should be a resource. */
    @SCIssue(issueSeverity=Error,description="A resource value is expected here, not a literal.")
    public static final Resource NotResource      = resource("NotResource");

    /** Error class for a redundant (unknown, unexpected) property of some node. */
    @SCIssue(issueSeverity=Warning,description="This property is unexpected.")
    public static final Resource Redundant        = resource("Redundant");

    /** Error class for a source that does not refer to a Turtle URI. */
    @SCIssue(issueSeverity=Error,description="The vocabulary or shape source does not appear to be in Turtle.")
    public static final Resource SourceNotTurtle  = resource("SourceNotTurtle");

    /** Error class for a vocab term that is not defined by its parent ontology URI. */
    @SCIssue(issueSeverity=Error,description="This term does not have rdfs:isDefinedBy its parent ontology.")
    public static final Resource TermNotInVocab   = resource("TermNotInVocab");

    /** Error class for an undefined term. */
    @SCIssue(issueSeverity=Warning,description="This resource is not defined in its parent vocabulary.")
    public static final Resource UndefinedTerm    = resource("UndefinedTerm");

    /** Error class for an unreachable resource. */
    @SCIssue(issueSeverity=Warning,description="This resource is not fetchable.")
    public static final Resource Unreachable      = resource("Unreachable");

    /** Error class for a property of the wrong type. */
    @SCIssue(issueSeverity=Error,description="Wrong type for oslc:valueType, or for a property definition, or for a literal.")
    public static final Resource WrongType        = resource("WrongType");


    /** Predicate for a check. */
    @SCTerm(type=TermType.Property,description="The subject of a check.")
    public static final Property checks          = property("checks");

    /** Predicate for an issue. */
    @SCTerm(type=TermType.Property,description="An issue reported by the checker.")
    public static final Property issue           = property("issue");

    /** Predicate for an issue count. */
    @SCTerm(type=TermType.Property,description="The overall number of issues reported by the checker within the current scope.")
    public static final Property issueCounts     = property("issueCounts");

    /** Predicate for an info count. */
    @SCTerm(type=TermType.Property,description="The number of info issues reported by the checker within the current scope.")
    public static final Property infoCount       = property("infoCount");

    /** Predicate for a warning count. */
    @SCTerm(type=TermType.Property,description="The number of warning issues reported by the checker within the current scope.")
    public static final Property warnCount       = property("warnCount");

    /** Predicate for an error count. */
    @SCTerm(type=TermType.Property,description="The number of error issues reported by the checker within the current scope.")
    public static final Property errorCount       = property("errorCount");

    /** Predicate for a result. */
    @SCTerm(type=TermType.Property,description="The check results for a set of resources within the current scope.")
    public static final Property result           = property("result");

    /** Predicate for the subject of an issue. */
    @SCTerm(type=TermType.Property,description="The subject of an issue.")
    public static final Property subject          = property("subject");

    /** Predicate for the value of an issue. */
    @SCTerm(type=TermType.Property,description="The value concerning which an issue is being reported.")
    public static final Property value            = property("value");

    /** Predicate for the severity of an issue. */
    @SCTerm(type=TermType.Property,description="The severity of an issue.")
    public static final Property severity         = property("severity");

    /** Predicate for the a singular crosscheck object. */
    @SCTerm(type=TermType.Property,description="The singular for a cross-check artifact.")
    public static final Property singular         = property("singular");

    /** Predicate for the a plural crosscheck object. */
    @SCTerm(type=TermType.Property,description="The plural for a cross-check artifact.")
    public static final Property plural         = property("plural");

    /** Crosscheck predicate for an undefined term. */
    @SCXCheck(severity=IssueSeverity.Error,singular="term",plural="terms",
            description="referenced in the given shapes, but not defined in the expected vocabulary:")
    public static final Property undefinedTerm    = property("undefinedTerm");

    /** Crosscheck predicate for an unreferenced term. */
    @SCXCheck(severity=IssueSeverity.Info,singular="term",plural="terms",
            description="defined in the given vocabularies, but not referenced in the given shapes:")
    public static final Property unusedTerm       = property("unusedTerm");

    /** Crosscheck predicate for an unreferenced vocabulary. */
    @SCXCheck(severity=IssueSeverity.Warning,singular="vocabulary",plural="vocabularies",
            description="given, but not referenced in the given shapes:")
    public static final Property unusedVocabulary = property("unusedVocabulary");
}
