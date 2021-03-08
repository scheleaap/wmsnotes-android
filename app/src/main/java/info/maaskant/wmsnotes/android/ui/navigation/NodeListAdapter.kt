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
import info.maaskant.wmsnotes.client.indexing.Folder
import info.maaskant.wmsnotes.client.indexing.Node
import info.maaskant.wmsnotes.client.indexing.Note
import kotlin.properties.Delegates
import kotlin.random.Random

internal class NodeListAdapter(
    private val listener: NodeListAdapterListener
) :
    RecyclerView.Adapter<NodeListAdapter.NodeViewHolder>(),
    AutoUpdatableAdapter {

    // Source: https://github.com/antoniolg/diffutil-recyclerview-kotlin/blob/master/app/src/main/java/com/antonioleiva/diffutilkotlin/ContentAdapter.kt
    var items: List<Node> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList) { o, n -> o.aggId == n.aggId }
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
        // to rely on our implementation in Note/Folder.hashCode()
        return items[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.node_list_item, parent, false)
        return NodeViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    interface NodeListAdapterListener {
        fun onClick(node: Node)
        fun onLongClick(node: Node)
    }

    class NodeViewHolder(
        view: View,
        private val listener: NodeListAdapterListener
    ) :
        RecyclerView.ViewHolder(view),
        View.OnClickListener,
        View.OnLongClickListener {
        private lateinit var node: Node
        private val main: LinearLayout = view.findViewById(R.id.node_item)
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val title: TextView = view.findViewById(R.id.title)

        init {
            view.setOnClickListener(this)
            view.setOnLongClickListener(this)
        }

        fun bind(node: Node) {
            this.node = node
            main.isActivated = Random.nextBoolean()
            title.text = node.title
            when (node) {
                is Note -> {
                    icon.setImageDrawable(
                        IconicsDrawable(
                            icon.context,
                            GoogleMaterial.Icon.gmd_insert_drive_file
                        )
                    )
                }
                is Folder -> {
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
            listener.onClick(node)
        }

        override fun onLongClick(view: View): Boolean {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            listener.onLongClick(node)
            return true
        }
    }
}
