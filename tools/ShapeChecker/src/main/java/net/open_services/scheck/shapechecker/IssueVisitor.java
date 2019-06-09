package net.open_services.scheck.shapechecker;

/**
 * A functional interface describing a visitor pattern for issues in a model.
 * @param <T> The type of the parent node
 * @param <E> The type of the child elements
 * @author Nick Crossley. Released to public domain 2019.
 */
@FunctionalInterface
interface IssueVisitor<T,E>
{
    /**
     * The actions to be taken when visiting a node in the model.
     * @param level the level of the child within the result tree
     * @param parentName the name of the parent
     * @param parent the parent of the node being visited
     * @param child the node being visited
     */
    void visit(int level,String parentName,T parent,E child);
}
