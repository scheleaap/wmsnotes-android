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

internal class NodeListAdapter : RecyclerView.Adapter<NodeListAdapter.NodeViewHolder>(),
    AutoUpdatableAdapter {

    // Source: https://github.com/antoniolg/diffutil-recyclerview-kotlin/blob/master/app/src/main/java/com/antonioleiva/diffutilkotlin/ContentAdapter.kt
    var items: List<Node> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList) { o, n -> o.aggId == n.aggId }
    }

    private lateinit var listener: NodeListAdapterListener

    init {
        setHasStableIds(true)
        this.items = emptyList()
    }

    fun getItem(position: Int): Node {
        return items[position]
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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.node_list_item, parent, false)
            .apply {
                setOnClickListener(listener::onClick)
                setOnLongClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    listener.onLongClick(it)
                }
            }
        return NodeViewHolder(v)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        val node = items[position]
        holder.main.isActivated = Random.nextBoolean()
        holder.title.text = node.title
        when (node) {
            is Note -> {
                holder.icon.setImageDrawable(
                    IconicsDrawable(
                        holder.icon.context,
                        GoogleMaterial.Icon.gmd_insert_drive_file
                    )
                )
            }
            is Folder -> {
                holder.icon.setImageDrawable(
                    IconicsDrawable(
                        holder.icon.context,
                        GoogleMaterial.Icon.gmd_folder
                    )
                )
            }
        }
    }

    // TODO: Move this to constructor argument?
    fun setListener(listener: NodeListAdapterListener) {
        this.listener = listener
    }

    interface NodeListAdapterListener {
        fun onClick(view: View): Unit
        fun onLongClick(view: View): Boolean
    }

    class NodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //  TODO: Consider storing adapter position here and returning that in the NodeListAdapterListener instead of the view
        val main: LinearLayout = view.findViewById(R.id.node_item)
        val icon: ImageView = view.findViewById(R.id.icon)
        val title: TextView = view.findViewById(R.id.title)
    }

}
