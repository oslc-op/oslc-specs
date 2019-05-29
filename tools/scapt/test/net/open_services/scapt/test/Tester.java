package net.open_services.scapt.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import net.open_services.scapt.SCTermModel;
import net.open_services.scapt.SCVocabModel;


/**
 * Simple test for use of Velocity in this annotation processor
 */
public class Tester
{
    private SCVocabModel vocabulary = null;
    private Map<String,SCTermModel> classes = new TreeMap<>();
    private Map<String,SCTermModel> properties = new TreeMap<>();
    private Map<String,SCTermModel> resources = new TreeMap<>();
    private Map<String,SCTermModel> issues = new TreeMap<>();

    /**
     * Main entry point to test program.
     * @param args none
     */
    public static void main(String[] args)
    {
        new Tester().run();
    }

    private void run()
    {
        vocabulary = new SCVocabModel(
            "http://open-services.net/ns/scheck",
            "scheck",
            "ShapeChecker vocabulary.",
            "A vocabulary for terms used in the result model of the OSLC Shape and Vocabulary checker.");

        classes.put("Class21", new SCTermModel("Class21","third class"));
        classes.put("Class2", new SCTermModel("Class2","second class"));
        classes.put("Class211", new SCTermModel("Class211","fourth class"));
        classes.put("Class1", new SCTermModel("Class1","first class"));
        classes.put("Class3a", new SCTermModel("Class3a","sixth class"));
        classes.put("Class3", new SCTermModel("Class3","fifth class"));
        properties.put("prop1", new SCTermModel("prop1","first property"));
        properties.put("prop2", new SCTermModel("prop2","second property"));
        properties.put("prop3", new SCTermModel("prop3","third property"));
        resources.put("res1", new SCTermModel("res1","one and only individual"));
        issues.put("issue1",new SCTermModel("issue1","issue #1"));
        issues.put("issue2",new SCTermModel("issue2","issue #2"));
        issues.put("issue3",new SCTermModel("issue3","issue #3"));
        issues.put("issue4",new SCTermModel("issue4","issue #4"));
        writeVocab();
    }


    private void writeVocab()
    {
        Properties props = new Properties();
        URL url = this.getClass().getClassLoader().getResource("velocity.properties");
        try
        {
            props.load(url.openStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        VelocityContext vc = new VelocityContext();
        Template vt = ve.getTemplate("templates/genVocab.vm");

        vc.put("vocab", vocabulary);
        if (!classes.isEmpty())
        {
            vc.put("classes", classes);
        }
        if (!properties.isEmpty())
        {
            vc.put("properties", properties);
        }
        if (!resources.isEmpty())
        {
            vc.put("resources", resources);
        }
        if (!issues.isEmpty())
        {
            vc.put("issues", issues);
        }

        System.out.println("Generating output from template " + vt.getName());
        try (Writer writer = new PrintWriter(System.out))
        {
            vt.merge(vc, writer);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
