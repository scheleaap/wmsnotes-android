package info.maaskant.wouttest2.detail;

import static io.reark.reark.utils.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.immutables.value.Value;

import android.support.annotation.NonNull;

import info.maaskant.wouttest2.data.NotebookStore;
import info.maaskant.wouttest2.model.ContentNode;
import io.reark.reark.data.DataStreamNotification;
import io.reark.reark.viewmodels.AbstractViewModel;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class DetailViewModel extends AbstractViewModel {

    @NonNull
    private final BehaviorSubject<ContentNode> contentNode = BehaviorSubject.create();

    @NonNull
    private final BehaviorSubject<String> contentNodeId = BehaviorSubject.create();

    @NonNull
    private final BehaviorSubject<ContentChange<String>> markdownChange = BehaviorSubject.create();

    @NonNull
    private final BehaviorSubject<String> htmlContent = BehaviorSubject.create();

    @NonNull
    private final NotebookStore notebookStore;

    public DetailViewModel(@NonNull final NotebookStore notebookStore) {
        this.notebookStore = requireNonNull(notebookStore);
    }

    @NonNull
    public Observable<ContentNode> getContentNode() {
        return contentNode;
    }

    public void setContentNodeId(@NonNull final String nodeId) {
        checkNotNull(nodeId);

        Timber.d("Changing content node to %s", nodeId);
        this.contentNodeId.onNext(nodeId);
    }

    @NonNull
    public Observable<String> getHtmlContent() {
        return htmlContent;
    }

    @NonNull
    public BehaviorSubject<ContentChange<String>> getMarkdownChange() {
        return markdownChange;
    }

    public void setMarkdownContentFromUser(@NonNull final String newMarkdownContent) {
        markdownChange.onNext(ImmutableContentChange.of(newMarkdownContent, true));
    }

    public void save() {
        if (this.contentNodeId.hasValue() && this.markdownChange.hasValue()) {
            this.notebookStore.setNodeContent(this.contentNodeId.getValue(),
                    this.markdownChange.getValue().getContent());
        }
    }

    @Override
    public void subscribeToDataStoreInternal(
            @NonNull final CompositeSubscription compositeSubscription) {
        checkNotNull(compositeSubscription);
        Timber.v("subscribeToDataStoreInternal");

        ConnectableObservable<DataStreamNotification<ContentNode>> getContentNodeSource = this.contentNodeId
                .distinctUntilChanged().observeOn(Schedulers.io())
                .switchMap(this.notebookStore::getContentNode).publish();

        ConnectableObservable<DataStreamNotification<String>> getNodeContentSource = this.contentNodeId
                .distinctUntilChanged().observeOn(Schedulers.io())
                .switchMap(this.notebookStore::getNodeContent).publish();

        compositeSubscription.add(getContentNodeSource.filter(DataStreamNotification::isOnNext)
                .map(DataStreamNotification::getValue).subscribe(this.contentNode));

        compositeSubscription.add(getNodeContentSource.filter(DataStreamNotification::isOnNext)
                .map(DataStreamNotification::getValue).map(i -> ImmutableContentChange.of(i, false))
                .subscribe(this.markdownChange));

        compositeSubscription.add(this.markdownChange.distinctUntilChanged()
                .observeOn(Schedulers.computation()).throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .map(i -> this.convertToHtml(i.getContent())).subscribe(this.htmlContent));

        compositeSubscription.add(getContentNodeSource.connect());
        compositeSubscription.add(getNodeContentSource.connect());
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @NonNull
    private String convertToHtml(@NonNull final String markdownContent) {
        requireNonNull(markdownContent);
        List<Extension> extensions = Arrays.asList(TablesExtension.create(),
                AutolinkExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions)
                .nodeRendererFactory(StylesheetHtmlNodeRenderer::new).build();

        org.commonmark.node.Node document = parser.parse(markdownContent);
        document.getFirstChild().insertBefore(new StylesheetNode("markdown.css"));
        String htmlContent = renderer.render(document);
        return htmlContent;
    }

    @Value.Immutable
    interface ContentChange<T> {

        @NonNull
        @Value.Parameter
        T getContent();

        @NonNull
        @Value.Parameter
        boolean isFromUser();

    }

}
