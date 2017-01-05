package info.maaskant.wouttest2.data;

import static com.annimon.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.annimon.stream.Stream;

import android.support.annotation.NonNull;

import info.maaskant.wouttest2.model.ContentNode;
import info.maaskant.wouttest2.model.FolderNode;
import info.maaskant.wouttest2.model.Node;
import io.reark.reark.data.DataStreamNotification;
import rx.Observable;
import timber.log.Timber;

public class NotebookRepository {

    public NotebookRepository() {
    }

    public Observable<DataStreamNotification<List<Node>>> getChildNodes(String parentNodeId) {
        List<Node> items = new ArrayList<>();
        File path = new File(parentNodeId);
        if (path.canRead()) {
            items = Stream.of(path.listFiles()).sorted().map(this::createNode).collect(toList());
        } else {
            Timber.d("Cannot read path %s", path);
        }

        return Observable.just(DataStreamNotification.onNext(items));
    }

    @NonNull
    private Node createNode(File path) {
        if (path.isDirectory()) {
            return new FolderNode(path.getAbsolutePath(), path.getName());
        } else {
            return new ContentNode(path.getAbsolutePath(), path.getName());
        }
    }

}
