package info.maaskant.wmsnotes.android.ui.navigation

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.View.GONE
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.ui.detail.DetailActivity
import info.maaskant.wmsnotes.android.ui.main.MainActivity
import info.maaskant.wmsnotes.android.ui.navigation.NavigationListAdapter.*
import info.maaskant.wmsnotes.android.ui.navigation.NavigationViewModel.FolderTitleValidity.Invalid
import info.maaskant.wmsnotes.android.ui.navigation.NavigationViewModel.FolderTitleValidity.Valid
import info.maaskant.wmsnotes.android.ui.util.OnBackPressedListener
import info.maaskant.wmsnotes.model.Path
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

@AndroidEntryPoint
class NavigationFragment @Inject constructor(
) : Fragment(), OnBackPressedListener, NavigationListAdapterListener {
    private val logger by logger()

    private val viewModel: NavigationViewModel by viewModels()

    private lateinit var mainDisposable: CompositeDisposable

    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var folderViewContainer: ViewGroup

    private lateinit var inflater: LayoutInflater

    private var foldersByPath: Map<Path, FolderContainer> = mapOf()
    private var folderDisposables: Map<Path, Disposable> = mapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.navigation_menu, menu)
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
        floatingActionButton.setOnClickListener {
            viewModel.createNote()?.let {
                openNote(it)
            }
        }
    }

    override fun onBackPressed(): Boolean =
        viewModel.navigateUp()

    private fun onCreateFolderClicked() {
        MaterialDialog(requireContext()).show {
            lifecycleOwner(this@NavigationFragment)
            title(res = R.string.create_folder_dialog_title)
            input(
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
                waitForPositiveButton = false
            ) { dialog, text ->
                val inputField = dialog.getInputField()
                val validity = viewModel.isValidFolderTitle(text.toString())
                inputField.error = when (validity) {
                    Valid -> null
                    is Invalid -> validity.reason
                }
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, validity is Valid)
            }
            positiveButton(R.string.create) { dialog ->
                viewModel.createFolder(dialog.getInputField().text.toString())
            }
            negativeButton(android.R.string.cancel)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create_folder -> {
            onCreateFolderClicked()
            true
        }
        else -> super.onOptionsItemSelected(item)
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
        logger.trace("Binding folder $path to view model")
        val listAdapter = (foldersByPath[path] ?: error("Folder $path not present")).listAdapter
        val disposable = viewModel.getNavigationItems(path)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { logger.trace("New folder contents: $it") }
            .subscribeBy(
                onNext = { listAdapter.items = it },
                onError = { logger.warn("Error", it) })
        folderDisposables = folderDisposables + (path to disposable)
    }

    private fun bindViewModel() {
        logger.trace("Binding to view model")
        unbindViewModel()
        mainDisposable = CompositeDisposable()
        mainDisposable.add(
            viewModel.getStack()
                .map { it.items }
                .doOnNext { logger.trace("New path stack: $it") }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::updateFoldersAccordingToStack) { logger.warn("Error", it) }
        )
        foldersByPath.keys.forEach(::bindFolderToViewModel)
    }

    private fun createAndAddFolder(path: Path) {
        logger.trace("Creating and adding folder $path")
        val listAdapter = NavigationListAdapter(this)
        val view = inflater.inflate(R.layout.navigation_folder, folderViewContainer, false).apply {
            visibility = GONE
            findViewById<RecyclerView>(R.id.node_list_view).apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
                itemAnimator = NavigationItemAnimator(context)
            }
        }
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

    override fun onClick(navigationItem: NavigationItem) {
        if (viewModel.isSelectionModeEnabled().blockingFirst()) {
            viewModel.toggleSelection(navigationItem)
        } else {
            when (navigationItem) {
                is Folder -> viewModel.navigateTo(navigationItem.path)
                is Note -> openNote(navigationItem.id)
            }
        }
    }

    override fun onLongClick(navigationItem: NavigationItem): Boolean {
        return viewModel.toggleSelection(navigationItem)
    }

    private fun openNote(aggId: String) {
        val context = requireContext()
        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra(DetailActivity.AGG_ID_KEY, aggId)
        context.startActivity(intent)
    }

    private fun removeFolder(path: Path) {
        logger.trace("Removing folder $path")
        unbindFolderFromViewModel(path)
        folderViewContainer.removeView(foldersByPath.getValue(path).view)
        foldersByPath = foldersByPath - path
    }

    private fun removeFoldersNotInStack(stack: List<Path>) {
        val foldersNotInList = foldersByPath.keys - stack
        foldersNotInList.forEach(::removeFolder)
    }

    private fun unbindFolderFromViewModel(path: Path) {
        logger.trace("Unbinding folder $path from view model")
        folderDisposables.getValue(path).dispose()
        folderDisposables = folderDisposables - path
    }

    private fun unbindViewModel() {
        logger.trace("Unbinding from view model")
        if (this::mainDisposable.isInitialized) {
            mainDisposable.dispose()
        }
        folderDisposables.values.toSet().forEach { it.dispose() }
    }

    private fun updateFoldersAccordingToStack(stack: List<Path>) {
        // TODO Replace ugly hack with something better
        (activity as MainActivity?)?.supportActionBar?.title =
            stack.lastOrNull()?.elements?.lastOrNull() ?: "WMS Notes"
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

    private data class FolderContainer(val view: View, val listAdapter: NavigationListAdapter)
}
