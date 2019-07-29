package net.open_services.scheck.shapechecker;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;


/**
 * Check that all vocabulary terms are used in some shape,
 * and that all shape classes, property predicates, and allowed value resources are defined in vocabularies.
 * @author Nick Crossley. Released to public domain 2015.
 */
public class CrossCheck
{
    private Map<Resource,Boolean> vocabs           = new HashMap<>();
    private Map<Resource,Boolean> termsInVocabs    = new HashMap<>();
    private Map<Resource,Boolean> termsInShapes    = new HashMap<>();
    private ResultModel           resultModel;


    /**
     * Construct a new CrossCheck.
     * @param resultModel the result model
     */
    public CrossCheck(ResultModel resultModel)
    {
        this.resultModel = resultModel;
    }


    /**
     * Build the maps of resources in vocabs and shapes.
     */
    public void buildMaps()
    {
        // Add vocabulary classes to vocabs map
        ResIterator vi = resultModel.getModel().listResourcesWithProperty(RDF.type, Terms.VocabResult);
        while (vi.hasNext())
        {
            StmtIterator ri = vi.next().listProperties(Terms.checks);
            while (ri.hasNext())
            {
                Resource res = ri.next().getObject().asResource();
                assert res != null;
                vocabs.put(res,false);
            }
        }

        // Add vocabulary terms to termsInVocab map
        ResIterator ti = resultModel.getModel().listResourcesWithProperty(RDF.type, Terms.TermResult);
        while (ti.hasNext())
        {
            final Resource res = ti.next().getPropertyResourceValue(Terms.checks);
            assert res != null;
            termsInVocabs.put(res,false);
        }

        // Add terms referenced by shapes to termsInShapes map
        Pattern regex = Pattern.compile("#.*$");
        StmtIterator si = resultModel.getModel().listStatements(null, DCTerms.references, (RDFNode) null);
        while (si.hasNext())
        {
            Resource ref = si.next().getObject().asResource();
            assert ref != null;
            Resource vocab = ResourceFactory.createResource(regex.matcher(ref.getURI()).replaceFirst("#"));
            if (vocabs.containsKey(vocab))
            {
                assert vocab != null;
                vocabs.put(vocab, true);
                termsInShapes.put(ref,false);
            }
        }

        // Cross reference refs into terms
        for (Resource res : termsInShapes.keySet())
        {
            if (termsInVocabs.containsKey(res))
            {
                assert res != null;
                termsInShapes.put(res, true);
                termsInVocabs.put(res, true);
            }
        }
    }


    /**
     * Check consistency of the vocabs and shapes.
     */
    public void check()
    {
        for (Map.Entry<Resource,Boolean> vocab : vocabs.entrySet())
        {
            if (!vocab.getValue())
            {
                resultModel.getSummary().addProperty(Terms.unusedVocabulary, vocab.getKey());
            }
        }

        for (Map.Entry<Resource,Boolean> term : termsInVocabs.entrySet())
        {
            if (!term.getValue())
            {
                resultModel.getSummary().addProperty(Terms.unusedTerm, term.getKey());
            }
        }

        for (Map.Entry<Resource,Boolean> term : termsInShapes.entrySet())
        {
            if (!term.getValue())
            {
                resultModel.getSummary().addProperty(Terms.undefinedTerm, term.getKey());
            }
        }
    }


    /**
     * Print a list of all vocabulary terms defined.
     */
    public void printVocabTerms()
    {

        if (!termsInVocabs.isEmpty())
        {
            // Show list of all vocabulary terms
            System.out.println("\nList of vocabulary terms:");
            termsInVocabs.keySet()
                .stream()
                .map(r->r.getLocalName())
                .sorted()
                .forEachOrdered(s->System.out.printf("   %s%n",s));
        }
    }
}

