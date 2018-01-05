package info.maaskant.wmsnotes.data;

import java.util.List;

import javax.inject.Inject;

import android.support.annotation.NonNull;

import info.maaskant.wmsnotes.model.ContentNode;
import info.maaskant.wmsnotes.model.Node;
import io.reark.reark.data.DataStreamNotification;
import rx.Observable;
import timber.log.Timber;

public class NotebookStore {

    private final NotebookRepository notebookRepository;

    @Inject
    NotebookStore(NotebookRepository notebookRepository) {
        this.notebookRepository = notebookRepository;
    }

    /**
     * Get the child nodes of a node.
     *
     * @param parentNodeId
     *            The identifier of the parent of the children, {@code null} to get the root node.
     * @return An {@link Observable} of {@link DataStreamNotification} of {@link List} of
     *         {@link Node}.
     */
    @NonNull
    public Observable<DataStreamNotification<List<Node>>> getChildNodes(
            @NonNull final String parentNodeId) {
        return Observable.just(
                DataStreamNotification.onNext(this.notebookRepository.getChildNodes(parentNodeId)));
    }

    /**
     * Get a content node.
     *
     * @param nodeId
     *            The identifier of the node.
     * @return An {@link Observable} of {@link DataStreamNotification} of {@link ContentNode}.
     */
    @NonNull
    public Observable<DataStreamNotification<ContentNode>> getContentNode(
            @NonNull final String nodeId) {
        try {
            return Observable.just(
                    DataStreamNotification.onNext(this.notebookRepository.getContentNode(nodeId)));
        } catch (Throwable t) {
            Timber.e(t);
            return Observable.just(DataStreamNotification.fetchingError());
        }
    }

    /**
     * Get the content of a node.
     *
     * @param nodeId
     *            The identifier of the node.
     * @return An {@link Observable} of {@link DataStreamNotification} of {@link String}.
     */
    @NonNull
    public Observable<DataStreamNotification<String>> getNodeContent(@NonNull final String nodeId) {
        try {
            return Observable.just(
                    DataStreamNotification.onNext(this.notebookRepository.getNodeContent(nodeId)));
        } catch (Throwable t) {
            Timber.e(t);
            return Observable.just(DataStreamNotification.fetchingError());
        }
    }

    /**
     * Set the content of a node.
     *
     * @param nodeId
     *            The identifier of the node.
     * @param content
     *            The new content.
     * @return An {@link Observable} of {@link DataStreamNotification} of {@link Void}.
     */
    @NonNull
    public Observable<DataStreamNotification<Void>> setNodeContent(@NonNull final String nodeId,
            @NonNull final String content) {
        try {
            this.notebookRepository.setNodeContent(nodeId, content);
            return Observable.just(DataStreamNotification.onNext(null));
        } catch (Throwable t) {
            Timber.e(t);
            return Observable.just(DataStreamNotification.fetchingError());
        }
    }

}
