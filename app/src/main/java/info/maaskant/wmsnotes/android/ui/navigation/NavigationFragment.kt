package info.maaskant.wmsnotes.android.ui.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.ui.detail.DetailActivity
import info.maaskant.wmsnotes.client.indexing.Folder
import info.maaskant.wmsnotes.client.indexing.Note
import info.maaskant.wmsnotes.model.CommandProcessor
import info.maaskant.wmsnotes.model.Path
import info.maaskant.wmsnotes.model.note.CreateNoteCommand
import javax.inject.Inject

class NavigationFragment : Fragment() {
    @Inject
    lateinit var commandProcessor: CommandProcessor

    @Inject
    lateinit var viewModel: NavigationViewModel

    private lateinit var nodeListAdapter: NodeListAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var recyclerView: RecyclerView

    private lateinit var floatingActionButton: FloatingActionButton

//    private var recyclerViewScrollEventObservable: Observable<RecyclerViewScrollEvent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nodeListAdapter = NodeListAdapter(emptyList())
        linearLayoutManager = LinearLayoutManager(context)
        return inflater.inflate(R.layout.navigation_fragment, container, false).apply {
            recyclerView = findViewById<RecyclerView>(R.id.node_list_view).apply {
                setHasFixedSize(true)
                layoutManager = linearLayoutManager
                adapter = nodeListAdapter;
            }
            floatingActionButton = findViewById<FloatingActionButton>(R.id.floating_action_button)
//        recyclerViewScrollEventObservable = RxRecyclerView.scrollEvents(recyclerView)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NavigationViewModel::class.java)
        viewModel.notes.observe(this, Observer {
            nodeListAdapter.items = it
        })
        nodeListAdapter.setOnClickListener(View.OnClickListener { clickedView ->
            val itemPosition = recyclerView.getChildAdapterPosition(clickedView)
            val node = nodeListAdapter.getItem(itemPosition)
            when (node) {
                is Folder -> viewModel.navigateTo(node)
                is Note -> {
                    val intent = Intent(clickedView.context, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.NODE_ID_KEY, node.aggId)
                    clickedView.getContext().startActivity(intent)
                }
            }
        })
        floatingActionButton.setOnClickListener {
            commandProcessor.commands.onNext(
                CreateNoteCommand(
                    aggId = null,
                    path = Path(),
                    title = getString(R.string.new_note_title),
                    content = ""
                )
            )
        }
    }
}
