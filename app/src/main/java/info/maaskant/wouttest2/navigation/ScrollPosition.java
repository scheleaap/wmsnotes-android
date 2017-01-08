package info.maaskant.wouttest2.navigation;

import android.support.annotation.NonNull;

import org.immutables.value.Value;

import info.maaskant.wouttest2.model.Node;

/**
 * Records the scroll position of a {@link NodeListView}. The scroll position consists of a
 * reference {@link info.maaskant.wouttest2.model.Node} and an offset.
 *
 * @see android.support.v7.widget.LinearLayoutManager#scrollToPositionWithOffset(int, int)
 */
@Value.Immutable
@Value.Style(init = "with*")
interface ScrollPosition {

    /**
     * The reference {@link Node}.
     */
    @NonNull
    @Value.Parameter
    Node getReference();

    /**
     * The distance (in pixels) between the start edge of the item view and start edge of the
     * RecyclerView.
     */
    @NonNull
    @Value.Parameter
    int getOffset();

}
