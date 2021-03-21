package info.maaskant.wmsnotes.android.ui.navigation

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.utilities.logger

// Sources:
// https://medium.com/swlh/recyclerview-item-change-animations-ebe2383bb481
// https://www.youtube.com/watch?v=imsr8NrIAMs
class NavigationItemAnimator(private val context: Context) : DefaultItemAnimator() {
    private val logger by logger()

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        return if (preInfo is NavigationItemHolderInfo && postInfo is NavigationItemHolderInfo && oldHolder is NavigationItemViewHolder) {
            logger.info("--- ${oldHolder == newHolder}")
//            val main = (AnimatorInflater.loadAnimator(
//                context,
//                if (postInfo.isSelected) R.animator.navigation_item_select else R.animator.navigation_item_deselect
//            ) as ObjectAnimator).apply { target = oldHolder.main }

            val iconFront = (AnimatorInflater.loadAnimator(
                context,
                if (postInfo.isSelected) R.animator.navigation_item_icon_flip_out else R.animator.navigation_item_icon_flip_in
            ) as AnimatorSet).apply { setTarget(oldHolder.iconFront) }

            val iconBack = (AnimatorInflater.loadAnimator(
                context,
                if (postInfo.isSelected) R.animator.navigation_item_icon_flip_in else R.animator.navigation_item_icon_flip_out
            ) as AnimatorSet).apply { setTarget(oldHolder.iconBack) }

            val animatorSet = (AnimatorSet()).apply {
                playTogether(/*main,*/ iconFront, iconBack)
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        dispatchAnimationFinished(oldHolder)
                    }
                })
            }
            animatorSet.start()

            return true
        } else {
            super.animateChange(oldHolder, newHolder, preInfo, postInfo)
        }
    }

//    private fun selectionColor(isSelected: Boolean) =
//        resources.getColor(
//            if (isSelected) {
//                R.color.navigation_item_selected_background
//            } else {
//                R.color.navigation_item_default_background
//            }
//        )

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean =
        true

    override fun recordPostLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder
    ): ItemHolderInfo =
        NavigationItemHolderInfo((viewHolder as NavigationItemViewHolder).navigationItem.isSelected)
            .setFrom(viewHolder)

    override fun recordPreLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder,
        changeFlags: Int,
        payloads: List<Any>
    ): ItemHolderInfo =
        if ((changeFlags and FLAG_CHANGED) != 0) {
            NavigationItemHolderInfo((viewHolder as NavigationItemViewHolder).navigationItem.isSelected)
                .setFrom(viewHolder)
        } else {
            super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads)
        }

    private data class NavigationItemHolderInfo(val isSelected: Boolean) :
        RecyclerView.ItemAnimator.ItemHolderInfo()
}
