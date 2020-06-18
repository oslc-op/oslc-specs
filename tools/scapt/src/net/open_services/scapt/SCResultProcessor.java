package net.open_services.scapt;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import net.open_services.scheck.annotations.*;


/**
 * Annotation Processor to build an RDF Vocabulary document from annotations in the ShapeChecker source.
 * @author Nick Crossley. Released to public domain, September 2015.
 */
@SupportedAnnotationTypes({
    "net.open_services.scheck.annotations.SCVocab",
    "net.open_services.scheck.annotations.SCTerm",
    "net.open_services.scheck.annotations.SCIssue",
    "net.open_services.scheck.annotations.SCXCheck"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SCResultProcessor extends AbstractProcessor
{
    private SCVocabModel            vocabulary = null;
    private Map<String,SCTermModel> classes    = new TreeMap<>();
    private Map<String,SCTermModel> properties = new TreeMap<>();
    private Map<String,SCTermModel> resources  = new TreeMap<>();
    private Map<String,SCTermModel> issues     = new TreeMap<>();
    private Map<String,SCTermModel> xchecks    = new TreeMap<>();
    private Map<String,SCTermModel> severities = new TreeMap<>();


    /**
     * Create new processor
     */
    public SCResultProcessor()
    {
        super();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round)
    {
        processSeverities();

        if (annotations.isEmpty())
        {
            return true;
        }

        for (Element element : round.getElementsAnnotatedWith(SCVocab.class))
        {
            processVocab(element);
        }
        for (Element element : round.getElementsAnnotatedWith(SCTerm.class))
        {
            processTerm(element);
        }
        for (Element element : round.getElementsAnnotatedWith(SCIssue.class))
        {
            processIssue(element);
        }
        for (Element element : round.getElementsAnnotatedWith(SCXCheck.class))
        {
            processXCheck(element);
        }

        writeVocab();
        return true;
    }


    /**
     * Process the severities enumeration
     */
    private void processSeverities()
    {
        for (IssueSeverity sev : IssueSeverity.values())
        {
            String s = sev.toString();
            SCTermModel sevTerm = new SCTermModel(s, s + " severity.");
            severities.put(s, sevTerm);
        }
    }


    /**
     * Process the vocabulary annotation.
     * @param element the annotated element to be processed; should be a type or package.
     */
    private void processVocab(Element element)
    {
        if (vocabulary != null)
        {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Duplicate SCVocab annotation on " + element.getKind() + " " + element.getSimpleName()
                    + ". The SCVocab annotation may only be applied once.");
        }
        SCVocab annotation = element.getAnnotation(SCVocab.class);
        vocabulary = new SCVocabModel(annotation.uri(),annotation.prefix(),annotation.domain(),annotation.description());
    }


    /**
     * Process a term to be added to the ShapeChecker vocabulary.
     * @param element the annotated element to be processed; must be a field
     */
    private void processTerm(Element element)
    {
        if (element.getKind() != ElementKind.FIELD)
        {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Invalid SCTerm annotation on " + element.getKind() + " " + element.getSimpleName()
                    + ". The SCTerm annotation may only be applied to fields.");
            return;
        }

        VariableElement varElement = (VariableElement) element;
        SCTerm termAnnotation = varElement.getAnnotation(SCTerm.class);
        SCTermModel property = new SCTermModel(varElement.getSimpleName().toString(),termAnnotation.description());
        switch (termAnnotation.type())
        {
        case Class:
            classes.put(property.getName(), property);
            break;
        case Property:
            properties.put(property.getName(), property);
            break;
        default:
            resources.put(property.getName(), property);
            break;
        }
    }


    /**
     * Process an issue enum value to be added to the ShapeChecker vocabulary.
     * @param element the annotated element to be processed; must be a field
     */
    private void processIssue(Element element)
    {
        if (element.getKind() != ElementKind.FIELD)
        {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Invalid SCIssue annotation on " + element.getKind() + " " + element.getSimpleName()
                    + ". The SCIssue annotation may only be applied to fields.");
        }

        VariableElement varElement = (VariableElement) element;
        SCIssue issueAnnotation = varElement.getAnnotation(SCIssue.class);
        SCTermModel issue = new SCTermModel(varElement.getSimpleName().toString(),
            issueAnnotation.description(),issueAnnotation.issueSeverity());
        issues.put(issue.getName(), issue);
    }


    /**
     * Process a cross-check issue to be added to the ShapeChecker vocabulary.
     * @param element the annotated element to be processed; must be a field
     */
    private void processXCheck(Element element)
    {
        if (element.getKind() != ElementKind.FIELD)
        {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "Invalid SCXCheck annotation on " + element.getKind() + " " + element.getSimpleName()
                    + ". The SCXCheck annotation may only be applied to fields.");
        }

        VariableElement varElement = (VariableElement) element;
        SCXCheck xcheckAnnotation = varElement.getAnnotation(SCXCheck.class);
        SCTermModel xcheck = new SCTermModel(varElement.getSimpleName().toString(),
            xcheckAnnotation.description(),
            xcheckAnnotation.severity(),
            xcheckAnnotation.singular(),
            xcheckAnnotation.plural());
        xchecks.put(xcheck.getName(), xcheck);
    }


    private void writeVocab()
    {
        try
        {
            Properties props = new Properties();
            try (InputStream reader = getClass().getClassLoader().getResourceAsStream("net/open_services/scapt/velocity.properties"))
            {
                props.load(reader);
            }
            VelocityEngine ve = new VelocityEngine();
            ve.init(props);
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
            if (!xchecks.isEmpty())
            {
                vc.put("xchecks", xchecks);
            }
            if (!severities.isEmpty())
            {
                vc.put("severities", severities);
            }

            FileObject vocabFile =
                    processingEnv.getFiler()
                        .createResource(StandardLocation.SOURCE_OUTPUT, "", "main/resources/SCVocabulary.ttl");

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "creating vocabulary file " + vocabFile.toUri() + " using  template " + vt.getName());

            try (Writer writer = vocabFile.openWriter())
            {
                vt.merge(vc, writer);
            }
        }
        catch (IOException ioe)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ioe.getLocalizedMessage());
        }
    }
}
