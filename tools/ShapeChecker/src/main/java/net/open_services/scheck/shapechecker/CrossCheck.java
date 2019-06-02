package net.open_services.scheck.shapechecker;

import java.util.HashMap;
import java.util.Map;

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
    private Map<Resource,Boolean> classesInShapes  = new HashMap<>();
    private Map<Resource,Boolean> internalProps    = new HashMap<>();
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
     * Optionally, print list of vocabulary terms.
     * @param printTerms if true, a list of vocabulary terms is printed
     */
    public void buildMaps(boolean printTerms)
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

        // Add shape classes to classesInShapes map
        ResIterator si = resultModel.getModel().listResourcesWithProperty(RDF.type, Terms.ShapeResult);
        while (si.hasNext())
        {
            StmtIterator ri = si.next().listProperties(Terms.checks);
            while (ri.hasNext())
            {
                Resource res = ri.next().getObject().asResource();
                assert res != null;
                classesInShapes.put(res,false);
            }
        }

        // Add internal property definitions to the internalProps map
        ResIterator pi = resultModel.getModel().listResourcesWithProperty(RDF.type, Terms.PropertyResult);
        while (pi.hasNext())
        {
            Resource propertyDefResult = pi.next();
            final Resource checkRes = propertyDefResult.getPropertyResourceValue(Terms.checks);
            if (checkRes != null)
            {
                final Resource vocab = ResourceFactory.createResource(checkRes.getURI().replaceFirst("#.*$","#"));
                if (vocabs.containsKey(vocab))
                {
                    assert vocab != null;
                    vocabs.put(vocab, true);
                    internalProps.put(checkRes,false);
                }
            }

            // Add internal value references to the internalProps map
            StmtIterator ri = propertyDefResult.listProperties(DCTerms.references);
            while (ri.hasNext())
            {
                final Resource refRes = ri.next().getObject().asResource();
                if (refRes != null)
                {
                    final Resource vocab = ResourceFactory.createResource(refRes.getURI().replaceFirst("#.*$","#"));
                    if (vocabs.containsKey(vocab))
                    {
                        assert vocab != null;
                        vocabs.put(vocab, true);
                        internalProps.put(refRes,false);
                    }
                }
            }
        }

        // Cross reference classes into terms
        for (Resource clss : classesInShapes.keySet())
        {
            if (termsInVocabs.containsKey(clss))
            {
                assert clss != null;
                classesInShapes.put(clss, true);
                termsInVocabs.put(clss, true);
            }
        }

        // Cross reference internal properties into terms
        for (Resource prop : internalProps.keySet())
        {
            if (termsInVocabs.containsKey(prop))
            {
                assert prop != null;
                internalProps.put(prop, true);
                termsInVocabs.put(prop, true);
            }
        }

        if (printTerms && !termsInVocabs.isEmpty())
        {
            // Show list of all vocabulary terms
            System.out.println("List of vocabulary terms:");
            termsInVocabs.keySet().stream().map(r->r.getLocalName()).sorted().forEachOrdered(s->System.out.println(s));
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

        for (Map.Entry<Resource,Boolean> clss : classesInShapes.entrySet())
        {
            if (!clss.getValue())
            {
                resultModel.getSummary().addProperty(Terms.undefinedClass, clss.getKey());
            }
        }

        for (Map.Entry<Resource,Boolean> prop : internalProps.entrySet())
        {
            if (!prop.getValue())
            {
                resultModel.getSummary().addProperty(Terms.undefinedProp, prop.getKey());
            }
        }
    }
}
