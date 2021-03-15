// Source: https://github.com/antoniolg/diffutil-recyclerview-kotlin/blob/master/app/src/main/java/com/antonioleiva/diffutilkotlin/AutoUpdatableAdapter.kt
package info.maaskant.wmsnotes.android.ui.navigation

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface AutoUpdatableAdapter<T> {

    fun getChangePayload(oldItem: T, newItem: T): Any? = null

    fun RecyclerView.Adapter<*>.autoNotify(old: List<T>, new: List<T>, compare: (T, T) -> Boolean) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                compare(old[oldItemPosition], new[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                old[oldItemPosition] == new[newItemPosition]

            override fun getOldListSize() = old.size

            override fun getNewListSize() = new.size

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
                this@AutoUpdatableAdapter.getChangePayload(
                    old[oldItemPosition],
                    new[newItemPosition]
                )
        })

        diff.dispatchUpdatesTo(this)
    }
}
