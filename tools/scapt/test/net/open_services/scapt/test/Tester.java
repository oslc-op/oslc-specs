package net.open_services.scapt.test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.reflect.ReflectionObjectHandler;

import net.open_services.scapt.SCTermModel;
import net.open_services.scapt.SCVocabModel;
import net.open_services.scheck.annotations.IssueSeverity;


/**
 * Simple test for use of Mustache in this annotation processor.
 */
@SuppressWarnings("javadoc")
public class Tester
{
    public SCVocabModel     vocabulary      = null;
    public boolean          classesOrIssues = false;
    public Set<SCTermModel> classes         = new HashSet<>();
    public Set<SCTermModel> properties      = new HashSet<>();
    public Set<SCTermModel> resources       = new HashSet<>();
    public Set<SCTermModel> issues          = new HashSet<>();
    public Set<SCTermModel> xchecks         = new HashSet<>();
    public Set<SCTermModel> severities      = new HashSet<>();


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
            "A vocabulary for terms used in the result model of the OSLC Shape and Vocabulary checker.",
            "# <extra1>", "# extra line 2");

        classes.add(new SCTermModel("Class21","third class"));
        classes.add(new SCTermModel("Class2","second class"));
        classes.add(new SCTermModel("Class211","fourth class"));
        classes.add(new SCTermModel("Class1","first class"));
        classes.add(new SCTermModel("Class3a","sixth class"));
        classes.add(new SCTermModel("Class3","fifth class"));
        properties.add(new SCTermModel("prop1","first property"));
        properties.add(new SCTermModel("prop2","second property"));
        properties.add(new SCTermModel("prop3","third property"));
        resources.add(new SCTermModel("res1","one and \"only\" individual"));
        issues.add(new SCTermModel("issue1","issue &1"));
        issues.add(new SCTermModel("issue2","issue \n#2"));
        issues.add(new SCTermModel("issue3","issue \\#3"));
        issues.add(new SCTermModel("issue4","issue '#4'"));
        severities.add(new SCTermModel("sev1","Severity 1",IssueSeverity.Warning));
        xchecks.add(new SCTermModel("xcheck1","Cross Check 1",IssueSeverity.Info,"check","checks"));
        writeVocab();
    }


    private void writeVocab()
    {
        DefaultMustacheFactory mf = new DefaultMustacheFactory();
        mf.setObjectHandler(new ReflectionObjectHandler() {
            @Override
            protected boolean areMethodsAccessible(Map<?, ?> map)
            {
                return true;
            }
            });
        Mustache mustache = mf.compile("templates/genVocab.mustache");

        classesOrIssues = !(classes.isEmpty() || issues.isEmpty());

        System.out.println("Generating output from template " + "templates/genVocab.mustache");
        try (Writer writer = new OutputStreamWriter(System.out,StandardCharsets.UTF_8))
        {
            mustache.execute(writer, this).flush();
        }
        catch (IOException e)
        {
            System.err.println("Failed to generate output");
            e.printStackTrace();
        }
    }
}
