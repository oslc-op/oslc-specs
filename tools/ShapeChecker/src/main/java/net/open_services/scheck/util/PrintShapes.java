package net.open_services.scheck.util;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import net.open_services.scheck.shapechecker.OSLC;

/**
 * Print a canonical representation of a set of OSLC resource shapes.
 *
 * Useful for a comparison after significant changes to the file structure,
 * use of blank nodes, changes to ordering, type of quotes, etc., to ensure
 * the semantic content is the same.
 *
 * @author Nick Crossley. Released to public domain 2020.
 */
public final class PrintShapes
{
    private Model       shapeModel;


    private PrintShapes()
    {
        shapeModel = ModelFactory.createDefaultModel();
    }


    /**
     * Produce a canonical text representation of a set of shapes in a set of files.
     * @param args the set of files to be processed
     */
    public static void main(String... args)
    {
        new PrintShapes().run(args);
    }

    private void run(String...args)
    {
        readShapeDocuments(args);
        printShapes();
    }


    private void readShapeDocuments(String... args)
    {
        for (String arg : args)
        {
            try
            {
                GlobExpander.checkFileOrURI(arg)
                    .stream()
                    .forEach(uri -> shapeModel.read(uri.toString(), "TURTLE"));
            }
            catch (URISyntaxException e)
            {
                System.err.println("Bad URI "+arg);
            }
        }
    }


    private void printShapes()
    {
        stmtStream(shapeModel.listStatements(null, RDF.type, OSLC.ResourceShape))
            .map(Statement::getSubject)
            .sorted((a,b)->a.getURI().compareTo(b.getURI()))
            .forEachOrdered(this::printShape);
    }


    private static Stream<Statement> stmtStream(StmtIterator iterator)
    {
        Iterable<Statement> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }


    private void printShape(Resource shape)
    {
        System.out.printf("<%s>%n\ta <%s>",shape.getURI(),"http://open-services.net/ns/core#ResourceShape");
        System.out.print(Stream.of(
                printString(shape,DCTerms.title),
                printString(shape,DCTerms.description),
                printURI(shape,OSLC.describes),
                printURI(shape,RDFS.seeAlso),
                printBoolean(shape,OSLC.hidden))
            .filter(s->!s.isEmpty())
            .map(s->"\t"+s)
            .collect(turtleCollector(" ;\n"," ;\n","")));

        System.out.print(
            stmtStream(shapeModel.listStatements(shape, OSLC.property, (RDFNode)null))
            .map(Statement::getResource)
            .sorted((a,b)->a.getRequiredProperty(OSLC.name).getString().compareTo(b.getRequiredProperty(OSLC.name).getString()))
            .map(this::printPropertyDef)
            .filter(s->!s.isEmpty())
            .collect(turtleCollector(" ;\n\t<oslc:property>\n\t\t[\n","\n\t\t] ,\n\t\t[\n","\n\t\t]")));
        System.out.println(" .\n");
    }


    private String printPropertyDef(Resource propDef)
    {
        return Stream.of(
                printString(propDef,OSLC.name),
                printString(propDef,DCTerms.title),
                printString(propDef,DCTerms.description),
                printURI(propDef,OSLC.propertyDefinition),
                printURI(propDef,OSLC.valueType),
                printURI(propDef,OSLC.representation),
                printURI(propDef,OSLC.occurs),
                printURI(propDef,OSLC.range),
                printURI(propDef,OSLC.allowedValue),
                printURI(propDef,OSLC.allowedValues),
                printURI(propDef,OSLC.defaultValue),
                printURI(propDef,OSLC.valueShape),
                printBoolean(propDef,OSLC.readOnly),
                printBoolean(propDef,OSLC.hidden),
                printBoolean(propDef,OSLC.isMemberProperty))
            .filter(s->!s.isEmpty())
            .map(s->"\t\t\t"+s)
            .collect(turtleCollector(""," ;\n",""));
    }


    private String printBoolean(Resource subject, Property predicate)
    {
        return stmtStream(shapeModel.listStatements(subject,predicate,(RDFNode)null))
            .map(Statement::getBoolean)
            .sorted(Comparator.naturalOrder())
            .map(b->b.toString())
            .collect(multivaluedPredicateCollector(predicate));
    }


    private String printURI(Resource subject, Property predicate)
    {
        return stmtStream(shapeModel.listStatements(subject,predicate,(RDFNode)null))
            .map(a->a.getResource().getURI())
            .sorted(Comparator.naturalOrder())
            .map(uri->"<"+uri+">")
            .collect(multivaluedPredicateCollector(predicate));
    }


    private String printString(Resource subject, Property predicate)
    {
        return stmtStream(shapeModel.listStatements(subject,predicate,(RDFNode)null))
            .map(Statement::getString)
            .sorted(Comparator.naturalOrder())
            .map(s->"\"\"\""+s.replace("\\", "\\\\")+"\"\"\"")
            .collect(multivaluedPredicateCollector(predicate));
    }


    private static Collector<CharSequence, ?, String> multivaluedPredicateCollector(Property predicate)
    {
        return Collector.of(
            () -> new StringJoiner(" , ", String.format("<%s> ", predicate.getURI()), "").setEmptyValue(""),
            StringJoiner::add,
            StringJoiner::merge,
            StringJoiner::toString);
    }


    private static Collector<CharSequence, ?, String> turtleCollector(String prefix,String middle,String suffix)
    {
        return Collector.of(
            () -> new StringJoiner(middle, prefix, suffix).setEmptyValue("WRONG"),
            StringJoiner::add,
            StringJoiner::merge,
            StringJoiner::toString);
    }
}


