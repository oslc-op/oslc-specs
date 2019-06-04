package net.open_services.scheck.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for a ShapeChecker Cross-check issue.
 * @author Nick Crossley. Released to public domain 2015.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SCXCheck
{
    /**
     * A human-readable description of the issue.
     * @return a human-readable description of the issue
     */
    String description();

    /**
     * The singular for a cross-check artifact..
     * @return the singular for a cross-check artifact.
     */
    String singular();

    /**
     * The plural for a cross-check artifact..
     * @return the plural for a cross-check artifact.
     */
    String plural();

    /**
     * The severity of the issue: Info, Warning, or Error.
     * The default value is Warning.
     * @return the severity of the issue
     */
    IssueSeverity severity() default IssueSeverity.Warning;
}
