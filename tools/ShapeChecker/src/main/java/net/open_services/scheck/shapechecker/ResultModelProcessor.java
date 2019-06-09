package net.open_services.scheck.shapechecker;

import java.util.function.Consumer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;


/**
 * A set of methods to process a {@link ResultModel} in some way.
 * @author Nick Crossley. Released to public domain 2015.
 */
public final class ResultModelProcessor
{
    private ResultModelProcessor()
    {
        // No instantiation
    }


    /**
     * Process the result model.
     * @param results               the result model to process
     * @param summaryPreProcessor   a function invoked to process the summary at the start of the overall processing
     * @param summaryPostProcessor  a function invoked to process the summary at the end of the overall processing
     * @param resultPreProcessor    a function invoked at the start of each sc:result node in the results
     * @param issueVisitor          a function invoked to process each issue in each vocab and shape
     * @param resultPostProcessor   a function invoked at the start of each sc:result node in the results
     */
    public static void processResults(
            ResultModel                     results,
            Consumer<Resource>              summaryPreProcessor,
            Consumer<Resource>              summaryPostProcessor,
            IssueVisitor<Resource,Resource> resultPreProcessor,
            IssueVisitor<Resource,Resource> issueVisitor,
            IssueVisitor<Resource,Resource> resultPostProcessor)
    {
        Model model = results.getModel();

        // Preprocess summary
        Resource summary = results.getSummary();
        summaryPreProcessor.accept(summary);

        // Process vocabulary results
        ResIterator ri = model.listResourcesWithProperty(RDF.type, Terms.VocabResult);
        while (ri.hasNext())
        {
            Resource vocabResult = ri.next();
            String vocab = vocabResult.getProperty(Terms.checks).getResource().getURI();
            resultPreProcessor.visit(0,vocab,summary,vocabResult);
            processOuterIssues(1,vocabResult,issueVisitor);
            processInnerIssues(1,vocabResult,vocab,resultPreProcessor,issueVisitor,resultPostProcessor);
            resultPostProcessor.visit(0,vocab,summary,vocabResult);
        }

        // Process shapes results
        ri = model.listResourcesWithProperty(RDF.type, Terms.ShapesResult);
        while (ri.hasNext())
        {
            Resource shapeResult = ri.next();
            String shapesFrom = shapeResult.getProperty(DCTerms.source).getResource().getURI();
            resultPreProcessor.visit(0,shapesFrom,summary,shapeResult);
            processOuterIssues(1,shapeResult,issueVisitor);
            processInnerIssues(1,shapeResult,shapesFrom,resultPreProcessor,issueVisitor,resultPostProcessor);
            resultPostProcessor.visit(0,shapesFrom,summary,shapeResult);
        }

        // Finally, post-process the summary
        summaryPostProcessor.accept(summary);
    }


    private static void processOuterIssues(int level,Resource outer,
            IssueVisitor<Resource,Resource> issueVisitor)
    {
        StmtIterator sti = outer.listProperties(Terms.issue);
        if (sti.hasNext())
        {
            while (sti.hasNext())
            {
                Statement st = sti.next();
                issueVisitor.visit(level,"",outer,st.getResource());
            }
        }
    }


    private static void processInnerIssues(
            int level,
            Resource start,
            String defaultParentName,
            IssueVisitor<Resource,Resource> preVisitor,
            IssueVisitor<Resource,Resource> issueVisitor,
            IssueVisitor<Resource,Resource> postVisitor)
    {
        StmtIterator sti1 = start.listProperties(Terms.result);
        while (sti1.hasNext())
        {
            Resource resultRes = sti1.next().getResource();
            Resource parent = resultRes.getPropertyResourceValue(Terms.checks);
            String parentName = parent == null ? defaultParentName : parent.getURI();

            preVisitor.visit(level,parentName,parent,resultRes);
            StmtIterator sti2 = resultRes.listProperties(Terms.issue);
            while (sti2.hasNext())
            {
                Statement st = sti2.next();
                issueVisitor.visit(level+1,parentName,resultRes,st.getResource());
            }
            postVisitor.visit(level,parentName,parent,resultRes);
            processInnerIssues(level+1,resultRes,parentName,preVisitor,issueVisitor,postVisitor);
        }
    }
}
