package net.open_services.scheck.util;

import static net.open_services.scheck.util.PrintUtils.printObject;
import static net.open_services.scheck.util.PrintUtils.turtleCollector;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

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
public final class PrintVocabs
{
    private static final Property VS_TERM_STATUS = ResourceFactory.createProperty("http://www.w3.org/2003/06/sw-vocab-status/ns#term_status");
    private static final Property PREF_NS        = ResourceFactory.createProperty("http://purl.org/vocab/vann/preferredNamespacePrefix");

    private Model       vocabModel;


    private PrintVocabs()
    {
        vocabModel = ModelFactory.createDefaultModel();
    }


    /**
     * Produce a canonical text representation of a set of shapes in a set of files.
     * @param args the set of files to be processed
     */
    public static void main(String... args)
    {
        new PrintVocabs().run(args);
    }

    private void run(String...args)
    {
        readVocabDocuments(args);
        printVocabs();
    }


    private void readVocabDocuments(String... args)
    {
        for (String arg : args)
        {
            try
            {
                GlobExpander.checkFileOrURI(arg)
                    .stream()
                    .forEach(uri -> vocabModel.read(uri.toString(), "TURTLE"));
            }
            catch (URISyntaxException e)
            {
                System.err.println("Bad URI "+arg);
            }
        }
    }


    private void printVocabs()
    {
        stmtStream(vocabModel.listStatements(null, RDF.type, OWL.Ontology))
            .map(Statement::getSubject)
            .sorted((a,b)->a.getURI().compareTo(b.getURI()))
            .forEachOrdered(this::printVocab);
    }


    private static Stream<Statement> stmtStream(StmtIterator iterator)
    {
        Iterable<Statement> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }


    private void printVocab(Resource vocab)
    {
        System.out.printf("<%s>%n\ta %s",vocab.getURI(),"owl:Ontology");
        System.out.print(Stream.of(
            	RDFS.label,
            	DCTerms.title,
                DCTerms.description,
                DCTerms.dateCopyrighted,
                DCTerms.license,
                DCTerms.source,
                PREF_NS,
                RDFS.seeAlso)
            .map(pred->printObject(vocabModel,vocab,pred))
            .filter(s->!s.isEmpty())
            .map(s->"\t"+s)
            .collect(turtleCollector(" ;\n"," ;\n"," .\n")));

        stmtStream(vocabModel.listStatements((Resource)null, RDFS.isDefinedBy, vocab))
            .map(Statement::getSubject)
            .sorted(Comparator.comparing(Resource::getURI))
            .forEachOrdered(this::printVocabTerm);
    }


    private void printVocabTerm(Resource term)
    {
    	System.out.printf("%n<%s>",term.getURI());
        System.out.println(Stream.of(
                RDF.type,
                OWL.sameAs,
                RDFS.subClassOf,
                RDFS.subPropertyOf,
                RDFS.range,
                RDFS.domain,
                RDFS.isDefinedBy,
            	RDFS.label,
                OSLC.inverseLabel,
            	RDFS.comment,
                VS_TERM_STATUS,
                OSLC.hidden,
                RDF.value,
                OSLC.impactType,
                SKOS.narrower,
                SKOS.broader,
        		RDFS.seeAlso)
            .map(pred->printObject(vocabModel,term,pred))
            .filter(s->!s.isEmpty())
            .map(s->"\t"+s)
            .collect(turtleCollector("\n"," ;\n"," .")));
    }
}
