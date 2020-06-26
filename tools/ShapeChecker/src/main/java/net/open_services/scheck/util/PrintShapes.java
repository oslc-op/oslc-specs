package net.open_services.scheck.util;

import static net.open_services.scheck.util.PrintUtils.printObject;
import static net.open_services.scheck.util.PrintUtils.stmtStream;
import static net.open_services.scheck.util.PrintUtils.turtleCollector;

import java.net.URISyntaxException;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
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
    	System.out.println("@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n");
        stmtStream(shapeModel.listStatements(null, RDF.type, OSLC.ResourceShape))
            .map(Statement::getSubject)
            .sorted((a,b)->a.getURI().compareTo(b.getURI()))
            .forEachOrdered(this::printShape);
    }


    private void printShape(Resource shape)
    {
        System.out.printf("<%s>%n\ta <%s>",shape.getURI(),"http://open-services.net/ns/core#ResourceShape");
        System.out.print(Stream.of(
                DCTerms.title,
                DCTerms.description,
                OSLC.describes,
                RDFS.seeAlso,
                OSLC.hidden)
            .map(pred->printObject(shapeModel,shape,pred))
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
        		Stream.of(
		        		OSLC.name,
		        		DCTerms.title,
		        		DCTerms.description,
		        		OSLC.propertyDefinition,
		        		OSLC.valueType,
		                OSLC.representation,
		                OSLC.occurs,
		                OSLC.range,
		                OSLC.allowedValue,
		                OSLC.defaultValue,
		                OSLC.valueShape,
		                OSLC.readOnly,
		                OSLC.hidden,
		                OSLC.maxSize,
		                OSLC.isMemberProperty)
					.map(pred->printObject(shapeModel,propDef,pred)),
				Stream.of(OSLC.allowedValues)
					.map(pred->printNestedObject(propDef,pred,OSLC.allowedValue)))
    		.flatMap(Function.identity())
            .filter(s->!s.isEmpty())
            .map(s->"\t\t\t"+s)
            .collect(turtleCollector(""," ;\n",""));
    }


    private String printNestedObject(Resource subject, Property predicate, Property subpred)
    {
        return stmtStream(shapeModel.listStatements(subject,predicate,(RDFNode)null))
        		.map(a->printObject(shapeModel,a.getResource(),subpred))
                .filter(s->!s.isEmpty())
        		.collect(turtleCollector(String.format("%s [ ",predicate.getURI())," ;\n"," ]"));
    }
}
