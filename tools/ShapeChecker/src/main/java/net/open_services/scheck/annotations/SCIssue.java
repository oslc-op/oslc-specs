package net.open_services.scheck.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for a ShapeChecker Issue.
 * @author Nick Crossley. Released to public domain 2015.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface SCIssue
{
    /**
     * A human-readable description of the issue.
     * @return a human-readable description of the issue
     */
    String description();

    /**
     * The severity of the issue: Info, Warning, or Error.
     * The default value is Warning.
     * @return the severity of the issue
     */
    IssueSeverity issueSeverity() default IssueSeverity.Warning;
}
