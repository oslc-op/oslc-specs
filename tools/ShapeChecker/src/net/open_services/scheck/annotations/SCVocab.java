package net.open_services.scheck.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for a ShapeChecker vocabulary.
 * @author Nick Crossley. Released to public domain, September 2015.
 */
@Target({ElementType.TYPE,ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface SCVocab
{
    /**
     * The full URI for the vocabulary.
     */
    String uri();

    /**
     * The preferred namespace prefix for the vocabulary.
     */
    String prefix();

    /**
     * A short title for the vocabulary/domain.
     */
    String domain();

    /**
     * A longer description of the vocabulary or domain
     */
    String description();
}
