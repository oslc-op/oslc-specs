package net.open_services.scheck.annotations;

/**
 * The severity of an issue.
 * @author Nick Crossley. Released to public domain 2019.
 */
public enum IssueSeverity
{
    /** Informational severity (the lowest). */
    Info,

    /** Warning severity. */
    Warning,

    /** Error severity (the highest real level). */
    Error,

    /** A special value for the reporting threshold, to show the summary only. */
    None;

    /**
     * Get an IssueSeverity enum value from its name.
     * @param name the name of an IssueSeverity
     * @return the named IssueSeverity
     * @throws IllegalArgumentException if {@code name} is not an IssueSeverity name
     */
    public static IssueSeverity findSeverity(String name)
    {
        return IssueSeverity.valueOf(name);
    }
}
