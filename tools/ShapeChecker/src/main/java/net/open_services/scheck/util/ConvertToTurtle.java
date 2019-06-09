package net.open_services.scheck.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Convert one or more RDF files from XML to Turtle.
 * @author Nick Crossley. Released to public domain 2015.
 */
public final class ConvertToTurtle
{
    private ConvertToTurtle()
    {
        // No instantiation
    }


    /**
     * Convert one or more RDF files from XML to Turtle.
     * @param args the path names of the files to be converted
     */
    public static void main(String[] args)
    {
        for (String arg : args)
        {
            if (!(new File(arg).exists()))
            {
                System.err.println("File not found: "+arg);
                continue;
            }

            String newName = arg.replaceAll("\\.(xml|rdf)$","") + ".ttl";
            System.out.println("Converting "+arg+" to "+newName);
            Model model = ModelFactory.createDefaultModel().read(arg);
            try (OutputStream os = Files.newOutputStream(Paths.get(newName)))
            {
                model.write(os, "TURTLE");
            }
            catch (IOException e)
            {
                System.err.println("Could not write "+newName);
            }
        }
    }
}
