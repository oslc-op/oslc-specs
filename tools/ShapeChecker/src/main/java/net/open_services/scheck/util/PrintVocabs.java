package net.open_services.scheck.util;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
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
            	printString(vocab,RDFS.label),
            	printString(vocab,DCTerms.title),
                printString(vocab,DCTerms.description),
                printString(vocab,DCTerms.dateCopyrighted),
                printString(vocab,PREF_NS),
                printURI(vocab,DCTerms.license),
                printURI(vocab,DCTerms.source),
                printURI(vocab,RDFS.seeAlso))
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
                printURI(term,RDF.type),
                printURI(term,OWL.sameAs),
                printURI(term,RDFS.subClassOf),
                printURI(term,RDFS.subPropertyOf),
                printURI(term,RDFS.range),
                printURI(term,RDFS.domain),
                printURI(term,RDFS.isDefinedBy),
            	printString(term,RDFS.label),
                printString(term,OSLC.inverseLabel),
            	printString(term,RDFS.comment),
                printString(term,VS_TERM_STATUS),
                printBoolean(term,OSLC.hidden),
                printString(term,RDF.value),
                printURI(term,OSLC.impactType),
                printURI(term,SKOS.narrower),
                printURI(term,SKOS.broader),
        		printURI(term,RDFS.seeAlso))
            .filter(s->!s.isEmpty())
            .map(s->"\t"+s)
            .collect(turtleCollector("\n"," ;\n"," .")));
    }


    private String printBoolean(Resource subject, Property predicate)
    {
        return stmtStream(vocabModel.listStatements(subject,predicate,(RDFNode)null))
            .map(Statement::getBoolean)
            .sorted(Comparator.naturalOrder())
            .map(b->b.toString())
            .collect(multivaluedPredicateCollector(predicate));
    }


    private String printURI(Resource subject, Property predicate)
    {
        return stmtStream(vocabModel.listStatements(subject,predicate,(RDFNode)null))
            .map(a->a.getResource().getURI())
            .sorted(Comparator.naturalOrder())
            .map(uri->"<"+uri+">")
            .collect(multivaluedPredicateCollector(predicate));
    }


    private String printString(Resource subject, Property predicate)
    {
        return stmtStream(vocabModel.listStatements(subject,predicate,(RDFNode)null))
            .map(Statement::getLiteral)
            .map(Literal::getLexicalForm)
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
