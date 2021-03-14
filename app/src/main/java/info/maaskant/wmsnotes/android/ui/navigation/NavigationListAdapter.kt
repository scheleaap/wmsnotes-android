package info.maaskant.wmsnotes.android.ui.navigation

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import info.maaskant.wmsnotes.R
import kotlin.properties.Delegates

internal class NavigationListAdapter(
    private val listener: NavigationItemListAdapterListener
) :
    RecyclerView.Adapter<NavigationListAdapter.NavigationItemViewHolder>(),
    AutoUpdatableAdapter {

    // Source: https://github.com/antoniolg/diffutil-recyclerview-kotlin/blob/master/app/src/main/java/com/antonioleiva/diffutilkotlin/ContentAdapter.kt
    var items: List<NavigationItem> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList) { o, n -> o.id == n.id }
    }

    init {
        setHasStableIds(true)
        this.items = emptyList()
    }

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

    override fun onBindViewHolder(holder: NavigationItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    interface NavigationItemListAdapterListener {
        fun onClick(navigationItem: NavigationItem)
        fun onLongClick(navigationItem: NavigationItem):Boolean
    }

    class NavigationItemViewHolder(
        view: View,
        private val listener: NavigationItemListAdapterListener
    ) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener,
        View.OnLongClickListener {
        private lateinit var navigationItem: NavigationItem
        private val main: LinearLayout = view.findViewById(R.id.navigation_item)
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val title: TextView = view.findViewById(R.id.title)

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        fun bind(navigationItem: NavigationItem) {
            this.navigationItem = navigationItem
            main.isActivated = navigationItem.isSelected
            when (navigationItem) {
                is Note -> {
                    title.text = navigationItem.title
                    icon.setImageDrawable(
                        IconicsDrawable(
                            icon.context,
                            GoogleMaterial.Icon.gmd_insert_drive_file
                        )
                    )
                }
                is Folder -> {
                    title.text = navigationItem.title
                    icon.setImageDrawable(
                        IconicsDrawable(
                            icon.context,
                            GoogleMaterial.Icon.gmd_folder
                        )
                    )
                }
            }
        }

        override fun onClick(view: View) {
            listener.onClick(navigationItem)
        }

        override fun onLongClick(view: View): Boolean {
            val consumed = listener.onLongClick(navigationItem)
            if (consumed) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            return consumed
        }
    }
}
