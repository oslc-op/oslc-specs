package net.open_services.scheck.shapechecker;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An exception representing an issue found by the shape checker.
 *
 * @author Nick Crossley. Released to public domain.
 */
public class ShapeCheckException extends Exception
{
    private static final long serialVersionUID = 1L;
    private Resource issueClass;
    private Resource subject;
    private RDFNode  object;


    /**
     * Construct a new ShapeCheckException with the given issue class.
     * @param issueClass a type of issue
     */
    public ShapeCheckException(Resource issueClass)
    {
        this.issueClass = issueClass;
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
