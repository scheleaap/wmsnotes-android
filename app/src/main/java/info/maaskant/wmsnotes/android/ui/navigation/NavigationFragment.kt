package info.maaskant.wmsnotes.android.ui.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
import info.maaskant.wmsnotes.android.ui.OnBackPressedListener
import info.maaskant.wmsnotes.android.ui.detail.DetailActivity
import info.maaskant.wmsnotes.client.indexing.Folder
import info.maaskant.wmsnotes.client.indexing.Note
import info.maaskant.wmsnotes.model.Path
import javax.inject.Inject

class NavigationFragment : Fragment(), OnBackPressedListener {
    @Inject
    lateinit var instrumentation: ApplicationInstrumentation

    @Inject
    lateinit var viewModel: NavigationViewModel

    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var folderViewContainer: ViewGroup

    private lateinit var inflater: LayoutInflater

    private var folderViewsByPath: Map<Path, View> = mapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        return inflater.inflate(R.layout.navigation_fragment, container, false).apply {
            floatingActionButton = findViewById(R.id.floating_action_button)
            folderViewContainer = findViewById(R.id.navigation_folder_container)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.restoreState(savedInstanceState)
        viewModel.getCurrentPath().observe(this, Observer { path ->
            createFolderViewIfNecessary(path)
            ensureOnlyOneChildIsVisible(folderViewContainer, folderViewsByPath.getValue(path))
        })
        floatingActionButton.setOnClickListener { viewModel.createNote() }
    }

    override fun onBackPressed(): Boolean {
        return if (this::viewModel.isInitialized) {
            viewModel.navigateUp()
        } else {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instrumentation.leakTracing.traceLeakage(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putAll(viewModel.getStateToSave())
        super.onSaveInstanceState(outState)
    }

    private fun createFolderViewIfNecessary(path: Path) {
        if (path !in folderViewsByPath) {
            val nodeListAdapter = NodeListAdapter()
            lateinit var recyclerView: RecyclerView
            val view = inflater.inflate(R.layout.navigation_folder, folderViewContainer, false).apply {
                visibility = GONE
                recyclerView = findViewById<RecyclerView>(R.id.node_list_view).apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(context)
                    adapter = nodeListAdapter;
                }
            }
            nodeListAdapter.setOnClickListener(NodeClickListener(recyclerView, nodeListAdapter, viewModel))
            viewModel.getNotes(path).observe(this, Observer {
                nodeListAdapter.items = it
            })
            folderViewsByPath = folderViewsByPath + (path to view)
            folderViewContainer.addView(view)
        }
    }

    companion object {
        private fun ensureOnlyOneChildIsVisible(parent: ViewGroup, child: View) {
            for (i in 0 until parent.childCount) {
                val otherView = parent.getChildAt(i)
                if (otherView != child) {
                    otherView.visibility = GONE
                }
            }
            child.visibility = View.VISIBLE
        }
    }

    private class NodeClickListener(
        private val recyclerView: RecyclerView,
        private val nodeListAdapter: NodeListAdapter,
        private val viewModel: NavigationViewModel
    ) : View.OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        override fun onClick(v: View) {
            val itemPosition = recyclerView.getChildAdapterPosition(v)
            val node = nodeListAdapter.getItem(itemPosition)
            when (node) {
                is Folder -> viewModel.navigateTo(node.path)
                is Note -> {
                    val intent = Intent(v.context, DetailActivity::class.java)
                    intent.putExtra(DetailActivity.NODE_ID_KEY, node.aggId)
                    v.context.startActivity(intent)
                }
            }
        }
    }
}
