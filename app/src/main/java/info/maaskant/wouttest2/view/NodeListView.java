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
package info.maaskant.wouttest2.view;

import static io.reark.reark.utils.Preconditions.checkNotNull;
import static io.reark.reark.utils.Preconditions.get;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import info.maaskant.wouttest2.R;
import info.maaskant.wouttest2.pojo.Node;
import info.maaskant.wouttest2.viewmodels.NavigationViewModel;
import io.reark.reark.utils.RxViewBinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

public class NodeListView extends FrameLayout {

    private RecyclerView nodeListRecyclerView;

    private NodeListAdapter nodeListAdapter;

    public NodeListView(Context context) {
        super(context, null);
    }

    public NodeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        nodeListAdapter = new NodeListAdapter(Collections.emptyList());

        nodeListRecyclerView = (RecyclerView) findViewById(R.id.node_list_recycler_view);
        nodeListRecyclerView.setHasFixedSize(true);
        nodeListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        nodeListRecyclerView.setAdapter(nodeListAdapter);
    }

    private void setNodes(@NonNull final List<Node> nodes) {
        checkNotNull(nodes);
        checkNotNull(nodeListAdapter);

        nodeListAdapter.set(nodes);
    }

    public static class ViewBinder extends RxViewBinder {
        private final NodeListView view;
        private final NavigationViewModel viewModel;

        public ViewBinder(@NonNull final NodeListView view,
                          @NonNull final NavigationViewModel viewModel) {
            this.view = get(view);
            this.viewModel = get(viewModel);
        }

        @Override
        protected void bindInternal(@NonNull final CompositeSubscription s) {
            s.add(viewModel.getNodes()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(view::setNodes));
            s.add(Observable.create(
                    subscriber -> {
                        view.nodeListAdapter.setOnClickListener(
                                this::nodeListAdapterOnClick);
                        subscriber.add(Subscriptions.create(() ->
                                view.nodeListAdapter.setOnClickListener(null)));
                    })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe());
        }

        private void nodeListAdapterOnClick(View clickedView) {
            final int itemPosition = view.nodeListRecyclerView.getChildAdapterPosition(clickedView);
            Node node = view.nodeListAdapter.getItem(itemPosition);
            viewModel.setCurrentParentNodeId(node.getId());
        }
    }
}
