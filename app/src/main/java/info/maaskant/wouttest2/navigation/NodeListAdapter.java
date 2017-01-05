package info.maaskant.wouttest2.navigation;

import static android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import info.maaskant.wouttest2.R;
import info.maaskant.wouttest2.model.Node;
import timber.log.Timber;

class NodeListAdapter extends RecyclerView.Adapter<NodeListAdapter.NodeViewHolder> {

    private final List<Node> nodes = new ArrayList<>();

    private OnClickListener onClickListener;

    public NodeListAdapter(List<Node> nodes) {
        setHasStableIds(true);
        this.nodes.addAll(nodes);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public Node getItem(int position) {
        return nodes.get(position);
    }

    @Override
    public NodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.node_list_item, parent,
                false);
        return new NodeViewHolder(v, onClickListener);
    }

    @Override
    public void onBindViewHolder(NodeViewHolder holder, int position) {
        holder.titleTextView.setText(nodes.get(position).getName());
        // Glide.with(holder.avatarImageView.getContext())
        // .load(gitHubRepositories.get(position).getOwner().getAvatarUrl())
        // .fitCenter()
        // .into(holder.avatarImageView);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    public void set(List<Node> nodes) {
        this.nodes.clear();
        this.nodes.addAll(nodes);

        notifyDataSetChanged();
    }

    public static class NodeViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        public final ImageView iconImageView;

        @NonNull
        public final TextView titleTextView;

        public NodeViewHolder(View view, OnClickListener onClickListener) {
            super(view);
            iconImageView = (ImageView) view.findViewById(R.id.icon);
            titleTextView = (TextView) view.findViewById(R.id.title);
            view.setOnClickListener(onClickListener);
        }
    }
}
