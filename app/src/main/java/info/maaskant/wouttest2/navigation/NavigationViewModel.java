package info.maaskant.wouttest2.navigation;

import static io.reark.reark.utils.Preconditions.checkNotNull;
import static io.reark.reark.utils.Preconditions.get;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Optional;

import android.support.annotation.NonNull;

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
    private final PublishSubject<String> currentParentNodeIdObservable = PublishSubject.create();

    @NonNull
    private final BehaviorSubject<List<Node>> nodes = BehaviorSubject.create();

    @NonNull
    private final BehaviorSubject<ProgressStatus> progressStatus = BehaviorSubject.create();
    @NonNull
    private final LinkedList<String> parentNodeIdHistory = new LinkedList<>();
    @NonNull
    private Optional<String> currentParentNodeId = Optional.absent();

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
    public Observable<List<Node>> getNodes() {
        return nodes.asObservable();
    }

    private void navigateTo(@NonNull final String parentNodeId, boolean addToHistory) {
        checkNotNull(parentNodeId);

        Timber.d("Navigating to %s", parentNodeId);
        this.currentParentNodeIdObservable.onNext(parentNodeId);
        if (addToHistory && this.currentParentNodeId.isPresent()) {
            this.parentNodeIdHistory.push(this.currentParentNodeId.get());
        }
        this.currentParentNodeId = Optional.of(parentNodeId);
    }


    public void navigateTo(@NonNull final String parentNodeId) {
        navigateTo(parentNodeId, true);
    }

    public boolean navigateBack() {
        if (!this.parentNodeIdHistory.isEmpty()) {
            this.navigateTo(this.parentNodeIdHistory.pop(), false);
            return true;
        } else {
            return false;
        }
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
                .subscribe(nodes));

        compositeSubscription.add(getChildNodesSource.connect());
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

}
