package net.open_services.scheck.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for a ShapeChecker vocabulary term.
 * @author Nick Crossley. Released to public domain 2015.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SCTerm
{
    /**
     * The type of the term: rdfs:Class, rdf:Property, or rdfs:Resource.
     */
    TermType type();

    /**
     * A human-readable description of the term.
     */
    String description();
}
