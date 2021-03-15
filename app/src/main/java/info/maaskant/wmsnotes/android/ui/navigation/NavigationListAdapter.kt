package info.maaskant.wmsnotes.android.ui.navigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import info.maaskant.wmsnotes.R
import kotlin.properties.Delegates

internal class NavigationListAdapter(
    private val listener: NavigationListAdapterListener
) :
    RecyclerView.Adapter<NavigationItemViewHolder>(),
    AutoUpdatableAdapter<NavigationItem> {

    // Source: https://github.com/antoniolg/diffutil-recyclerview-kotlin/blob/master/app/src/main/java/com/antonioleiva/diffutilkotlin/ContentAdapter.kt
    var items: List<NavigationItem> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList) { o, n -> o.id == n.id }
    }

    init {
        setHasStableIds(true)
        this.items = emptyList()
    }

    override fun getChangePayload(oldItem: NavigationItem, newItem: NavigationItem): Any =
        oldItem to newItem

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        // This is not really correct, since two different objects may have the same hash code. However, we are going
        // to rely on our implementation in NavigationItem.hashCode()
        return items[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.navigation_item, parent, false)
        return NavigationItemViewHolder(view, listener)
    }

    override fun onBindViewHolder(
        holder: NavigationItemViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        val navigationItem = items[position]
        if (payloads.isEmpty()) {
            holder.bind(navigationItem)
        } else {
            TODO("Unexpected payload in onBindViewHolder: $payloads")
        }
    }

    override fun onBindViewHolder(holder: NavigationItemViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    interface NavigationListAdapterListener {
        fun onClick(navigationItem: NavigationItem)
        fun onLongClick(navigationItem: NavigationItem): Boolean
    }
}
