package info.maaskant.wmsnotes.android.ui.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.client.indexing.Folder
import info.maaskant.wmsnotes.android.client.indexing.Node
import info.maaskant.wmsnotes.android.client.indexing.Note
import kotlin.properties.Delegates

internal class NodeListAdapter(initialItems: List<Node>) : RecyclerView.Adapter<NodeListAdapter.NodeViewHolder>(),
    AutoUpdatableAdapter {

    // Source: https://github.com/antoniolg/diffutil-recyclerview-kotlin/blob/master/app/src/main/java/com/antonioleiva/diffutilkotlin/ContentAdapter.kt
    var items: List<Node> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList) { o, n -> o.nodeId == n.nodeId }
    }

    private lateinit var onClickListener: OnClickListener

    init {
        setHasStableIds(true)
        this.items = initialItems
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun getItem(position: Int): Node {
        return items[position]
    }

    fun getPosition(node: Node): Int {
        return items.indexOf(node)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.node_list_item, parent,
            false
        )
        return NodeViewHolder(v, onClickListener)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        val node = items[position]
        holder.titleTextView.text = node.title
        when (node) {
            is Note -> {
                holder.iconImageView.setImageResource(R.drawable.ic_insert_drive_file_black_24dp)
            }
            is Folder -> {
                holder.iconImageView.setImageResource(R.drawable.ic_folder_black_24dp)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class NodeViewHolder(view: View, onClickListener: OnClickListener) : RecyclerView.ViewHolder(view) {
        val iconImageView: ImageView = view.findViewById<View>(R.id.icon) as ImageView
        val titleTextView: TextView = view.findViewById<View>(R.id.title) as TextView

        init {
            view.setOnClickListener(onClickListener)
        }
    }

}
