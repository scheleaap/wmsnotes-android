package info.maaskant.wmsnotes.android.ui.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.client.indexing.Folder
import info.maaskant.wmsnotes.client.indexing.Node
import info.maaskant.wmsnotes.client.indexing.Note
import kotlin.properties.Delegates

internal class NodeListAdapter : RecyclerView.Adapter<NodeListAdapter.NodeViewHolder>(),
    AutoUpdatableAdapter {

    // Source: https://github.com/antoniolg/diffutil-recyclerview-kotlin/blob/master/app/src/main/java/com/antonioleiva/diffutilkotlin/ContentAdapter.kt
    var items: List<Node> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList) { o, n -> o.aggId == n.aggId }
    }

    private lateinit var onClickListener: OnClickListener

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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.node_list_item, parent, false).apply {
            setOnClickListener(onClickListener)
        }
        return NodeViewHolder(v)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        val node = items[position]
        holder.titleTextView.text = node.title
        when (node) {
            is Note -> {
                holder.iconImageView.setImageDrawable(
                    IconicsDrawable(
                        holder.iconImageView.context,
                        GoogleMaterial.Icon.gmd_insert_drive_file
                    )
                )
            }
            is Folder -> {
                holder.iconImageView.setImageDrawable(
                    IconicsDrawable(
                        holder.iconImageView.context,
                        GoogleMaterial.Icon.gmd_folder
                    )
                )
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    class NodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconImageView: ImageView = view.findViewById(R.id.icon)
        val titleTextView: TextView = view.findViewById(R.id.title)
    }

}
