package info.maaskant.wmsnotes.navigation;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;

import org.immutables.value.Value;

import java.util.List;

import info.maaskant.wmsnotes.model.Node;

/**
 * The state of navigation, represented by a parent node, a list of {@link Node}s and a
 * {@link ScrollPosition}.
 */
@Value.Immutable
@Value.Style(init = "with*")
interface NavigationState {

    @NonNull
    @Value.Parameter
    String getParentNodeId();

    @NonNull
    @Value.Parameter
    List<Node> getNodes();

    @NonNull
    @Value.Parameter
    Optional<ScrollPosition> getScrollPosition();

}
