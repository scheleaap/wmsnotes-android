package info.maaskant.wouttest2.model;

import android.support.annotation.NonNull;

/**
 * Visitor interface for {@link Node}.
 */
public interface NodeVisitor {

    /**
     * Visit a {@link ContentNode}.
     *
     * @param node
     *            The node to visit.
     */
    void visit(@NonNull final ContentNode node);

    /**
     * Visit a {@link FolderNode}.
     *
     * @param node
     *            The node to visit.
     */
    void visit(@NonNull final FolderNode node);

}
