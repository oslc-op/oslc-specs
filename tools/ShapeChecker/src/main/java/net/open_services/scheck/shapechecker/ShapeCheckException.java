package net.open_services.scheck.shapechecker;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * An exception representing an issue found by the shape checker.
 *
 * @author Nick Crossley. Released to public domain 2015.
 */
public class ShapeCheckException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final transient Resource issueClass;
    private final transient Resource subject;
    private final transient RDFNode  object;


    /**
     * Construct a new ShapeCheckException with the given issue class.
     * @param issueClass a type of issue
     */
    public ShapeCheckException(Resource issueClass)
    {
        this.issueClass = issueClass;
        this.subject = null;
        this.object = null;
    }


    /**
     * Construct a new ShapeCheckException with the given issue class, subject, and object.
     * @param issueClass a type of issue
     * @param subject the subject of the issue
     * @param object the object or value of the issue
     */
    public ShapeCheckException(Resource issueClass, Resource subject, RDFNode object)
    {
        this.issueClass = issueClass;
        this.subject = subject;
        this.object = object;
    }


    /**
     * Construct a new ShapeCheckException with the given issue class, subject, object, and cause.
     * @param issueClass a type of issue
     * @param subject the subject of the issue
     * @param object the object or value of the issue
     * @param e an underlying exception
     */
    public ShapeCheckException(Resource issueClass, Resource subject, RDFNode object, Exception e)
    {
        super(e);
        this.issueClass = issueClass;
        this.subject = subject;
        this.object = object;
    }


    /**
     * Get the issueClass.
     * @return returns the issueClass.
     */
    public Resource getIssueClass()
    {
        return issueClass;
    }


    /**
     * Get the subject.
     * @return returns the subject.
     */
    public Resource getSubject()
    {
        return subject;
    }


    /**
     * Get the object.
     * @return returns the object.
     */
    public RDFNode getObject()
    {
        return object;
    }
}
