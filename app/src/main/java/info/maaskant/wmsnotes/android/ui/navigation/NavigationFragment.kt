package info.maaskant.wmsnotes.android.ui.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.leakcanary.LeakCanary
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
import info.maaskant.wmsnotes.android.ui.OnBackPressedListener
import info.maaskant.wmsnotes.android.ui.detail.DetailActivity
import info.maaskant.wmsnotes.client.indexing.Folder
import info.maaskant.wmsnotes.client.indexing.Note
import info.maaskant.wmsnotes.model.Path
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class NavigationFragment : Fragment(), OnBackPressedListener {
    @Inject
    lateinit var instrumentation: ApplicationInstrumentation

    @Inject
    lateinit var viewModel: NavigationViewModel

    private lateinit var mainDisposable: CompositeDisposable

    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var folderViewContainer: ViewGroup

    private lateinit var inflater: LayoutInflater

    private var foldersByPath: Map<Path, FolderContainer> = mapOf()
    private var folderDisposables: Map<Path, Disposable> = mapOf()

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
// TODO: LEAK TESTING
//        instrumentation.leakTracing.traceLeakage(this)
        val refWatcher = LeakCanary.installedRefWatcher()
        refWatcher.watch(this)
    }

    override fun onPause() {
        super.onPause()
        unbindViewModel()
    }

    override fun onResume() {
        super.onResume()
        bindViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putAll(viewModel.getStateToSave())
        super.onSaveInstanceState(outState)
    }

    private fun bindFolderToViewModel(path: Path) {
        val listAdapter = (foldersByPath[path] ?: error("Folder $path not present")).listAdapter
        val disposable = viewModel.getNotes(path)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { listAdapter.items = it }
        folderDisposables = folderDisposables + (path to disposable)
    }

    private fun bindViewModel() {
        unbindViewModel()
        mainDisposable = CompositeDisposable()
        mainDisposable.add(
            viewModel.getStack()
                .map { it.items }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::updateFoldersAccordingToStack)
        )
        foldersByPath.keys.forEach(::bindFolderToViewModel)
    }

    private fun createAndAddFolder(path: Path) {
        val listAdapter = NodeListAdapter()
        lateinit var recyclerView: RecyclerView
        val view = inflater.inflate(R.layout.navigation_folder, folderViewContainer, false).apply {
            visibility = GONE
            recyclerView = findViewById<RecyclerView>(R.id.node_list_view).apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter;
            }
        }
        listAdapter.setOnClickListener(NodeClickListener(recyclerView, listAdapter, viewModel))
        foldersByPath = foldersByPath + (path to FolderContainer(view, listAdapter))
        folderViewContainer.addView(view)
        bindFolderToViewModel(path)
    }

    private fun ensureFolderExistsAndIsVisible(path: Path) {
        if (path !in foldersByPath) {
            createAndAddFolder(path)
        }
        ensureOnlyOneChildIsVisible(folderViewContainer, foldersByPath.getValue(path).view)
    }

    private fun removeFolder(path: Path) {
        unbindFolderFromViewModel(path)
        folderViewContainer.removeView(foldersByPath.getValue(path).view)
        foldersByPath = foldersByPath - path
    }

    private fun removeFoldersNotInStack(stack: List<Path>) {
        val foldersNotInList = foldersByPath.keys - stack
        foldersNotInList.forEach(::removeFolder)
    }

    private fun unbindFolderFromViewModel(path: Path) {
        folderDisposables.getValue(path).dispose()
        folderDisposables = folderDisposables - path
    }

    private fun unbindViewModel() {
        if (this::mainDisposable.isInitialized) {
            mainDisposable.dispose()
        }
        folderDisposables.values.toSet().forEach { it.dispose() }
    }

    private fun updateFoldersAccordingToStack(stack: List<Path>) {
        removeFoldersNotInStack(stack)
        ensureFolderExistsAndIsVisible(stack.last())
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

    private data class FolderContainer(val view: View, val listAdapter: NodeListAdapter)
}
