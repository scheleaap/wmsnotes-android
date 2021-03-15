package info.maaskant.wmsnotes.android.ui.navigation

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import info.maaskant.wmsnotes.utilities.logger

// Source: https://medium.com/swlh/recyclerview-item-change-animations-ebe2383bb481
class NavigationItemAnimator : DefaultItemAnimator() {
    private val logger by logger()

    override fun canReuseUpdatedViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        payloads: List<Any>
    ): Boolean {
        val decision = true
//            super.canReuseUpdatedViewHolder(viewHolder, payloads)
        logger.info("--- CRUVH: $payloads -> $decision ---")
        return decision
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        val decision =true
//            super.canReuseUpdatedViewHolder(viewHolder)
        logger.info("--- CRUVH: -> $decision---")
        return decision
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        logger.info("--- animateChange: $oldHolder, $newHolder, $preInfo, $postInfo ---")
        return if (preInfo is NavigationItemHolderInfo) {
            val viewHolder = newHolder as NavigationItemViewHolder
            true
        } else {
            super.animateChange(oldHolder, newHolder, preInfo, postInfo)
        }
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        TODO()
    }

    @Suppress("UNCHECKED_CAST")
    override fun recordPreLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder,
        changeFlags: Int,
        payloads: List<Any>
    ): ItemHolderInfo {
        logger.info("--- recordPreLayoutInformation: $changeFlags, $payloads ---")
        return if (changeFlags and FLAG_CHANGED != 0 || changeFlags and FLAG_APPEARED_IN_PRE_LAYOUT != 0) {
            val old =
                (payloads.first() as Pair<NavigationItem, NavigationItem>).first
            val new =
                (payloads.last() as Pair<NavigationItem, NavigationItem>).second
            NavigationItemHolderInfo(old, new).setFrom(viewHolder)
        } else {
            super.recordPreLayoutInformation(
                state,
                viewHolder,
                changeFlags,
                payloads
            )
        }
    }

    private data class NavigationItemHolderInfo(val old: NavigationItem, val new: NavigationItem) :
        RecyclerView.ItemAnimator.ItemHolderInfo()
}
