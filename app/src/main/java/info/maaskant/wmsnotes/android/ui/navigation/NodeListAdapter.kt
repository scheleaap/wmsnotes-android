package info.maaskant.wmsnotes.android.ui.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.model.Folder
import info.maaskant.wmsnotes.android.model.Node
import info.maaskant.wmsnotes.android.model.Note

internal class NodeListAdapter(nodes: List<Node>) : RecyclerView.Adapter<NodeListAdapter.NodeViewHolder>() {

    private val nodes = ArrayList<Node>()

    private lateinit var onClickListener: OnClickListener

    init {
        setHasStableIds(true)
        this.nodes.addAll(nodes)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun getItem(position: Int): Node {
        return nodes[position]
    }

    fun getPosition(node: Node): Int {
        return nodes.indexOf(node)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.node_list_item, parent,
            false
        )
        return NodeViewHolder(v, onClickListener)
    }

    override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
        val node = nodes[position]
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
        return nodes.size
    }

    fun set(nodes: List<Node>) {
        // TODO: Alternative: https://github.com/guenodz/livedata-recyclerview-sample/blob/master/app/src/main/java/me/guendouz/livedata_recyclerview/PostsAdapter.java#L55-L67
        this.nodes.clear()
        this.nodes.addAll(nodes)
        notifyDataSetChanged()
    }

    class NodeViewHolder(view: View, onClickListener: OnClickListener) : RecyclerView.ViewHolder(view) {
        val iconImageView: ImageView = view.findViewById<View>(R.id.icon) as ImageView
        val titleTextView: TextView = view.findViewById<View>(R.id.title) as TextView
        init {
            view.setOnClickListener(onClickListener)
        }
    }

}
