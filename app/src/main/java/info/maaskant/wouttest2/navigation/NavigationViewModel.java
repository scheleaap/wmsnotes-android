package info.maaskant.wouttest2.navigation;

import static io.reark.reark.utils.Preconditions.checkNotNull;
import static io.reark.reark.utils.Preconditions.get;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;

import android.support.annotation.NonNull;

import org.immutables.value.Value;

import info.maaskant.wouttest2.data.DataFunctions;
import info.maaskant.wouttest2.model.Node;
import io.reark.reark.data.DataStreamNotification;
import io.reark.reark.viewmodels.AbstractViewModel;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class NavigationViewModel extends AbstractViewModel {

    @NonNull
    private final DataFunctions.GetChildNodes getChildNodes;
    @NonNull
    private final BehaviorSubject<String> currentParentNodeIdObservable = BehaviorSubject.create();
    @NonNull
    @Deprecated
    private final BehaviorSubject<List<Node>> nodes = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<NavigationState> navigationState = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<ProgressStatus> progressStatus = BehaviorSubject.create();
    @NonNull
    @Deprecated
    private final LinkedList<String> parentNodeIdHistory = new LinkedList<>();
    @NonNull
    private final LinkedList<HistoryItem> history = new LinkedList<>();
    @NonNull
    private Optional<String> currentParentNodeId = Optional.absent();
    private Optional<ScrollPosition> currentScrollPosition = Optional.absent();

    public NavigationViewModel(@NonNull final DataFunctions.GetChildNodes getChildNodes) {
        this.getChildNodes = get(getChildNodes);
    }

    @NonNull
    private static Func1<DataStreamNotification<List<Node>>, ProgressStatus> toProgressStatus() {
        return notification -> {
            if (notification.isFetchingStart()) {
                return ProgressStatus.LOADING;
            } else if (notification.isFetchingError()) {
                return ProgressStatus.ERROR;
            } else {
                return ProgressStatus.IDLE;
            }
        };
    }

    @NonNull
    @Deprecated
    public Observable<List<Node>> getNodes() {
        return nodes.asObservable();
    }

    public Observable<NavigationState> getNavigationState() {
        return this.navigationState.asObservable();
    }

    public void navigateForward(@NonNull final String parentNodeId) {
        checkNotNull(parentNodeId);

        Timber.d("Navigating to %s", parentNodeId);
        if (this.currentParentNodeId.isPresent()) {
            // TODO: Check optional
            this.history.push(ImmutableHistoryItem.of(this.currentParentNodeId.get(), this.currentScrollPosition));
        }
        this.currentParentNodeId = Optional.of(parentNodeId);
        this.currentScrollPosition = Optional.absent();
        this.currentParentNodeIdObservable.onNext(parentNodeId);
    }

    public boolean navigateBack() {
        if (!this.history.isEmpty()) {
            HistoryItem historyItem = this.history.pop();

            Timber.d("Navigating to %s", historyItem.getParentNodeId());
            this.currentParentNodeId = Optional.of(historyItem.getParentNodeId());
            this.currentScrollPosition = historyItem.getScrollPosition();
            this.currentParentNodeIdObservable.onNext(historyItem.getParentNodeId());

            return true;
        } else {
            return false;
        }

    }

    public void setScrollPosition(@NonNull final Optional<ScrollPosition> scrollPosition) {
        Timber.v("setScrollPosition(%s)", scrollPosition);
        this.currentScrollPosition = scrollPosition;
    }

    @Override
    public void subscribeToDataStoreInternal(
            @NonNull final CompositeSubscription compositeSubscription) {
        checkNotNull(compositeSubscription);
        Timber.v("subscribeToDataStoreInternal");

        ConnectableObservable<DataStreamNotification<List<Node>>> getChildNodesSource = this.currentParentNodeIdObservable
                .distinctUntilChanged().switchMap(getChildNodes::call).publish();

        compositeSubscription.add(getChildNodesSource.map(toProgressStatus())
                .doOnNext(progressStatus -> Timber.d("Progress status: %s", progressStatus.name()))
                .subscribe(this::setProgressStatus));

        compositeSubscription.add(getChildNodesSource.map(DataStreamNotification::getValue)
                        .doOnNext(nodes -> Timber.d("%s child nodes available", nodes.size()))
                        .map(this::createNavigationState)
                        .doOnNext(navigationState -> Timber.v("New navigation state: %s", navigationState))
                        .subscribe(this.navigationState)
//                .subscribe(nodes)
        );

        compositeSubscription.add(getChildNodesSource.connect());
    }

    /**
     * Creates a navigation state from a list of nodes.
     * <em>Warning:</em> This only works correctly if one request is processed at a time.
     *
     * @param nodes The {@link List} of {@link Node}s.
     * @return A {@link NavigationState} containing the nodes.
     */
    private NavigationState createNavigationState(List<Node> nodes) {
        return ImmutableNavigationState.builder().withParentNodeId(this.currentParentNodeId.get()).withNodes(nodes).withScrollPosition(this.currentScrollPosition).build();
    }

    @NonNull
    public Observable<ProgressStatus> getProgressStatus() {
        return progressStatus.asObservable();
    }

    public void setProgressStatus(@NonNull final ProgressStatus status) {
        checkNotNull(status);

        progressStatus.onNext(status);
    }

    public enum ProgressStatus {
        LOADING, ERROR, IDLE
    }

    @Value.Immutable
    interface HistoryItem {

        @NonNull
        @Value.Parameter
        String getParentNodeId();

        @NonNull
        @Value.Parameter
        Optional<ScrollPosition> getScrollPosition();

    }

}
