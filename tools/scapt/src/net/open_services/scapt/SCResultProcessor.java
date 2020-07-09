package net.open_services.scapt;

import java.io.IOException;
import java.io.Writer;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.reflect.ReflectionObjectHandler;

import net.open_services.scheck.annotations.IssueSeverity;
import net.open_services.scheck.annotations.SCIssue;
import net.open_services.scheck.annotations.SCTerm;
import net.open_services.scheck.annotations.SCVocab;
import net.open_services.scheck.annotations.SCXCheck;


/**
 * Annotation Processor to build an RDF Vocabulary document from annotations in the ShapeChecker source.
 * @author Nick Crossley. Released to public domain, September 2015.
 */
@SupportedAnnotationTypes({
    "net.open_services.scheck.annotations.SCVocab",
    "net.open_services.scheck.annotations.SCTerm",
    "net.open_services.scheck.annotations.SCIssue",
    "net.open_services.scheck.annotations.SCXCheck"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SuppressWarnings("javadoc")
public class SCResultProcessor extends AbstractProcessor
{
    private static final Comparator<SCTermModel> COMPARATOR = Comparator.comparing(SCTermModel::getName);

    public SCVocabModel     vocabulary = null;
    public Set<SCTermModel> classes    = new TreeSet<>(COMPARATOR);
    public Set<SCTermModel> properties = new TreeSet<>(COMPARATOR);
    public Set<SCTermModel> resources  = new TreeSet<>(COMPARATOR);
    public Set<SCTermModel> issues     = new TreeSet<>(COMPARATOR);
    public Set<SCTermModel> xchecks    = new TreeSet<>(COMPARATOR);
    public Set<SCTermModel> severities = new TreeSet<>(COMPARATOR);
    public boolean          classesOrIssues;



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
     * Process the severities enumeration.
     */
    private void processSeverities()
    {
        for (IssueSeverity sev : IssueSeverity.values())
        {
            String s = sev.toString();
            SCTermModel sevTerm = new SCTermModel(s, s + " severity.");
            severities.add(sevTerm);
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
        vocabulary = new SCVocabModel(annotation.uri(),annotation.prefix(),
            annotation.domain(),annotation.description(),annotation.additionalStatements());
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
            classes.add(property);
            break;
        case Property:
            properties.add(property);
            break;
        default:
            resources.add(property);
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
        issues.add(issue);
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
        xchecks.add(xcheck);
    }


    private void writeVocab()
    {
        try
        {
            DefaultMustacheFactory mf = new DefaultMustacheFactory();
            mf.setObjectHandler(new ReflectionObjectHandler() {
                @Override
                protected boolean areMethodsAccessible(Map<?,?> map)
                {
                    return true;
                }
                });
            Mustache mustache = mf.compile("templates/genVocab.mustache");

            classesOrIssues = !(classes.isEmpty() || issues.isEmpty());

            FileObject vocabFile =
                    processingEnv.getFiler()
                        .createResource(StandardLocation.SOURCE_OUTPUT, "", "SCVocabulary.ttl");

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                "creating vocabulary file " + vocabFile.toUri() + " using  template " + mustache.getName());

            try (Writer writer = vocabFile.openWriter())
            {
                mustache.execute(writer, this).flush();
            }
        }
        catch (IOException ioe)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ioe.getLocalizedMessage());
        }
    }


    /**
     * Escape characters in strings.
     * @param s the string to be escaped
     * @return the escaped string
     */
    public static String enquote(String s)
    {
        return s == null ? null :
           s.replace("\\", "\\\\")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\f", "\\f")
            .replace("\"", "\\\"");
    }

}
