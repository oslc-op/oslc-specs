package net.open_services.scheck.util;

import java.util.Comparator;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;


/**
 * Utility methods to aid in printing Turtle from a Jena model.
 * @author Nick Crossley. Released to public domain 2019.
 */
public final class PrintUtils
{
    private static String  prefixFormat  = "@prefix\\s+([A-Za-z0-9_]+:)\\s+<([^>]+)>\\s*+\\.?";
    private static Pattern prefixPattern = Pattern.compile(prefixFormat);

    private static Map<String, String> prefixes = Stream
        .of(new String[][] {
                {"dcterms:", "http://purl.org/dc/terms/"},
                {"foaf:",    "http://xmlns.com/foaf/0.1/"},
                {"ldp:",     "http://www.w3.org/ns/ldp#"},
                {"oslc:",    "http://open-services.net/ns/core#"},
                {"owl:",     "http://www.w3.org/2002/07/owl#"},
                {"rdf:",     "http://www.w3.org/1999/02/22-rdf-syntax-ns#"},
                {"rdfs:",    "http://www.w3.org/2000/01/rdf-schema#"},
                {"vann:",    "http://purl.org/vocab/vann/"},
                {"vs:",      "http://www.w3.org/2003/06/sw-vocab-status/ns#"},
                {"xsd:",     "http://www.w3.org/2001/XMLSchema#"}})
        .collect(Collectors.toMap(data->data[0], data->data[1], (x,y)->x, TreeMap::new));


    private PrintUtils()
    {
        // No instantiation
    }


    /**
     * Convert a Jena statement iterator into a Java 8 stream.
     * @param iterator the statement iterator
     * @return a stream of statements
     */
    public static Stream<Statement> stmtStream(StmtIterator iterator)
    {
        Iterable<Statement> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }


    /**
     * Return a string containing a set of Turtle triples describing the values of a property for a subject.
     * @param model the model containing the subject
     * @param subject the subject whose properties are to be shown
     * @param predicate the predicate property
     * @return a string containing a set of Turtle lines
     */
    public static String printObject(Model model, Resource subject, Property predicate)
    {
        return stmtStream(model.listStatements(subject,predicate,(RDFNode)null))
            .map(a->a.getObject().visitWith(new TurtlePrintVisitor()).toString())
            .sorted(Comparator.naturalOrder())
            .collect(multivaluedPredicateCollector(predicate));
    }


    /**
     * A collector to construct a predicate and a comma-separated list of values.
     * @param predicate the predicate
     * @return the predicate and its values
     */
    public static Collector<CharSequence, ?, String> multivaluedPredicateCollector(Property predicate)
    {
        return Collector.of(
            () -> new StringJoiner(" , ", String.format("%s ", prefix(predicate.getURI())), "").setEmptyValue(""),
            StringJoiner::add,
            StringJoiner::merge,
            StringJoiner::toString);
    }


    /**
     * A collector to join a set of lines in Turtle.
     * @param prefix the prefix
     * @param middle the separator
     * @param suffix the suffix
     * @return the joined string
     */
    public static Collector<CharSequence, ?, String> turtleCollector(String prefix,String middle,String suffix)
    {
        return Collector.of(
            () -> new StringJoiner(middle, prefix, suffix).setEmptyValue(""),
            StringJoiner::add,
            StringJoiner::merge,
            StringJoiner::toString);
    }


    /**
     * Return a string containing the prefix definitions.
     * @return the prefix definitions in a string
     */
    public static String printPrefixes()
    {
        StringBuilder buffer = new StringBuilder();
        for (Map.Entry<String,String> prefix : prefixes.entrySet())
        {
            buffer.append("@prefix ");
            buffer.append(prefix.getKey());
            buffer.append(" <");
            buffer.append(prefix.getValue());
            buffer.append("> .\n");
        }
        buffer.append("\n");
        return buffer.toString();
    }


    /**
     * Convert a fully-qualified URI into a prefixed string, if possible.
     * @param uri the URI string to be converted
     * @return the prefixed string, or the original string in angle brackets if a prefix was not found
     */
    public static String prefix(String uri)
    {
        for (Map.Entry<String,String> prefix : prefixes.entrySet())
        {
            if (uri.startsWith(prefix.getValue()))
            {
                return prefix.getKey() + uri.replace(prefix.getValue(), "");
            }
        }
        return "<" + uri + ">";
    }


    /**
     * Add a new prefix definition.
     * @param prefixDefinition a string of the form {@code @prefix pref: <uri> .}
     */
    public static void addPrefix(String prefixDefinition)
    {
        Matcher matcher = prefixPattern.matcher(prefixDefinition);
        if (matcher.matches())
        {
            prefixes.put(matcher.group(1), matcher.group(2));
        }
        else
        {
            System.err.println("Ignoring bad prefix definition "+prefixDefinition);
        }
    }
}
