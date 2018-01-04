package info.maaskant.wouttest2.data;

import static com.annimon.stream.Collectors.toList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.annimon.stream.Stream;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import android.support.annotation.NonNull;

import info.maaskant.wouttest2.model.ContentNode;
import info.maaskant.wouttest2.model.FolderNode;
import info.maaskant.wouttest2.model.Node;
import timber.log.Timber;

public class NotebookRepository {

    private static final Comparator<File> FILE_TYPE_COMPARATOR = (f1, f2) -> {
        if (f1.isDirectory() && !f2.isDirectory()) {
            return -1;
        } else if (!f1.isDirectory() && f2.isDirectory()) {
            return 1;
        } else {
            return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
        }
    };

    private static final int OTHER_DELAY_MILLISECONDS = 0;
    private static final int CONTENT_DELAY_MILLISECONDS = 0;// 3000;

    NotebookRepository() {
    }

    @NonNull
    private static ContentNode createContentNode(File path) {
        if (!path.isFile()) {
            throw new IllegalArgumentException(String.format("%s is not a file", path));
        }
        String title = path.getName().substring(0, path.getName().lastIndexOf("."));
        return new ContentNode(path.getAbsolutePath(), title);
    }

    @NonNull
    private static Node createNode(File path) {
        if (path.isDirectory()) {
            return new FolderNode(path.getAbsolutePath(), path.getName());
        } else {
            return createContentNode(path);
        }
    }

    // TODO: Temp
    private static void debugDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Timber.d(e);
        }
    }

    @NonNull
    List<Node> getChildNodes(@NonNull final String parentNodeId) {
        debugDelay(OTHER_DELAY_MILLISECONDS);
        requireNonNull(parentNodeId);

        List<Node> items = new ArrayList<>();
        File path = new File(parentNodeId);
        if (path.canRead()) {
            items = Stream.of(path.listFiles()).sorted(FILE_TYPE_COMPARATOR)
                    .map(NotebookRepository::createNode).collect(toList());
        } else {
            Timber.d("Cannot read path %s", path);
        }

        return items;
    }

    @NonNull
    ContentNode getContentNode(@NonNull final String nodeId) {
        debugDelay(OTHER_DELAY_MILLISECONDS);
        return createContentNode(new File(nodeId));
    }

    @NonNull
    String getNodeContent(@NonNull final String nodeId) {
        debugDelay(CONTENT_DELAY_MILLISECONDS);
        requireNonNull(nodeId);

        File path = new File(nodeId);
        try {
            return Files.toString(path, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not read file %s", nodeId), e);
        }
    }
}
