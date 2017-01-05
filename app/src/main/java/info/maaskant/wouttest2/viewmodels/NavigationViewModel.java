/*
 * The MIT License
 *
 * Copyright (c) 2013-2016 reark project contributors
 *
 * https://github.com/reark/reark/graphs/contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.maaskant.wouttest2.viewmodels;

import static io.reark.reark.utils.Preconditions.checkNotNull;
import static io.reark.reark.utils.Preconditions.get;

import java.util.List;

import android.support.annotation.NonNull;

import info.maaskant.wouttest2.data.DataFunctions;
import info.maaskant.wouttest2.pojo.Node;
import io.reark.reark.data.DataStreamNotification;
import io.reark.reark.utils.Log;
import io.reark.reark.viewmodels.AbstractViewModel;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class NavigationViewModel extends AbstractViewModel {

    private static final String TAG = NavigationViewModel.class.getSimpleName();
    @NonNull
    private final DataFunctions.GetChildNodes getChildNodes;
    @NonNull
    private final PublishSubject<String> currentParentNodeId = PublishSubject.create();
    @NonNull
    private final BehaviorSubject<List<Node>> nodes = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<ProgressStatus> progressStatus = BehaviorSubject.create();

    public NavigationViewModel(@NonNull final DataFunctions.GetChildNodes getChildNodes) {
        this.getChildNodes = get(getChildNodes);
    }

    @NonNull
    static Func1<DataStreamNotification<List<Node>>, ProgressStatus> toProgressStatus() {
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

    public void setCurrentParentNodeId(@NonNull final String currentParentNodeId) {
        checkNotNull(currentParentNodeId);

        this.currentParentNodeId.onNext(currentParentNodeId);
    }

    @Override
    public void subscribeToDataStoreInternal(@NonNull final CompositeSubscription compositeSubscription) {
        checkNotNull(compositeSubscription);
        Log.v(TAG, "subscribeToDataStoreInternal");

        ConnectableObservable<DataStreamNotification<List<Node>>> getChildNodesSource =
                this.currentParentNodeId.distinctUntilChanged().
                        switchMap(getChildNodes::call).publish();

        compositeSubscription.add(getChildNodesSource
                .map(toProgressStatus())
                .doOnNext(progressStatus -> Log.d(TAG, "Progress status: " + progressStatus.name()))
                .subscribe(this::setProgressStatus));

        compositeSubscription
                .add(getChildNodesSource.map(DataStreamNotification::getValue).
                        subscribe(nodes));

        compositeSubscription.add(getChildNodesSource.connect());
    }

    @NonNull
    public Observable<ProgressStatus> getProgressStatus() {
        return progressStatus.asObservable();
    }

    void setProgressStatus(@NonNull final ProgressStatus status) {
        checkNotNull(status);

        progressStatus.onNext(status);
    }

    public enum ProgressStatus {
        LOADING, ERROR, IDLE
    }

}
