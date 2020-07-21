package net.open_services.scheck.util;

import java.util.Comparator;
import java.util.StringJoiner;
import java.util.stream.Collector;
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
     * Print a set of Turtle triples describing the values of a property for a subject.
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
            () -> new StringJoiner(" , ", String.format("<%s> ", predicate.getURI()), "").setEmptyValue(""),
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

}
