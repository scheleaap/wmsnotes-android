package info.maaskant.wouttest2.data;

import static com.annimon.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.annimon.stream.Stream;

import android.util.Log;

import info.maaskant.wouttest2.pojo.Node;
import io.reark.reark.data.DataStreamNotification;
import rx.Observable;

public class NotebookRepository  {

    private static final String TAG = NotebookRepository.class.getName();

    public NotebookRepository() {
    }

    public Observable<DataStreamNotification<List<Node>>> getChildNodes(String parentNodeId) {
        List<Node> items = new ArrayList<>();
        File path = new File(parentNodeId);
        if (path.canRead()) {
            items = Stream.of(path.listFiles()).sorted().map(i -> new Node(i.getAbsolutePath(), i.getName())).collect(toList());
        } else {
            Log.d(TAG, "Cannot read path: " + path);
        }

        return Observable.just(DataStreamNotification.onNext(items));
    }
}
