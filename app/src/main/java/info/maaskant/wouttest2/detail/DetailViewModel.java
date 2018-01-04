package info.maaskant.wouttest2.detail;

import static io.reark.reark.utils.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

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
    private final BehaviorSubject<String> markdown = BehaviorSubject.create();

    @NonNull
    private final BehaviorSubject<String> html = BehaviorSubject.create();

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
    public Observable<String> getHtml() {
        return html;
    }

    @NonNull
    public BehaviorSubject<String> getMarkdown() {
        return markdown;
    }

    @Override
    public void subscribeToDataStoreInternal(
            @NonNull final CompositeSubscription compositeSubscription) {
        checkNotNull(compositeSubscription);
        Timber.v("subscribeToDataStoreInternal");

        ConnectableObservable<DataStreamNotification<ContentNode>> getContentNodeSource = this.contentNodeId
                .distinctUntilChanged().observeOn(Schedulers.io())
                .switchMap(this.notebookStore::getContentNode).publish();

        ConnectableObservable<DataStreamNotification<String>> getMarkdownContentSource = this.contentNodeId
                .distinctUntilChanged().observeOn(Schedulers.io())
                .switchMap(this.notebookStore::getNodeContent).publish();

        compositeSubscription.add(getContentNodeSource.filter(DataStreamNotification::isOnNext)
                .map(DataStreamNotification::getValue).subscribe(this.contentNode));

        compositeSubscription.add(getMarkdownContentSource.filter(DataStreamNotification::isOnNext)
                .map(DataStreamNotification::getValue).subscribe(this.markdown));

        compositeSubscription.add(getMarkdownContentSource.filter(DataStreamNotification::isOnNext)
                .map(DataStreamNotification::getValue).map(this::convertToHtml)
                .subscribe(this.html));

        compositeSubscription.add(getContentNodeSource.connect());
        compositeSubscription.add(getMarkdownContentSource.connect());
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

}
