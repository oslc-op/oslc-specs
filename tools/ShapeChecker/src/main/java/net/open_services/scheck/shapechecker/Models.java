package net.open_services.scheck.shapechecker;

import java.io.PrintStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

/**
 * Utilities for Jena models.
 * @author Nick Crossley. Released to public domain 2015.
 */
public final class Models
{
    /**
     * No instantiation.
     */
    private Models() {}


    /**
     * Print out a model.
     * @param model the model to be written
     * @param out the output stream
     */
    public static void write(Model model,PrintStream out)
    {
        RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
    }
}
