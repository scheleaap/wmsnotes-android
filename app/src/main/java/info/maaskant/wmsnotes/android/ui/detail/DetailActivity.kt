package info.maaskant.wmsnotes.android.ui.detail

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
import info.maaskant.wmsnotes.android.client.synchronization.SynchronizationTaskLifecycleObserver
import info.maaskant.wmsnotes.client.synchronization.SynchronizationTask
import timber.log.Timber
import javax.inject.Inject

class DetailActivity : AppCompatActivity(), HasSupportFragmentInjector {

    companion object {
        const val NODE_ID_KEY = "nodeId"
        private const val EDITOR_FRAGMENT_KEY = "editorFragment"
        private const val VIEWER_FRAGMENT_KEY = "viewerFragment"
    }

    @Inject
    lateinit var detailViewModel: DetailViewModel

    @Inject
    lateinit var detailController: DetailController

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var instrumentation: ApplicationInstrumentation

    @Inject
    lateinit var synchronizationTask: SynchronizationTask

    private lateinit var editorFragment: EditorFragment

    private lateinit var viewerFragment: ViewerFragment

    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.v(
            "onCreate (hash: %s, savedInstanceState: %s)",
            System.identityHashCode(this),
            savedInstanceState
        )
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        val noteId = if (intent.hasExtra(NODE_ID_KEY)) {
            intent.getStringExtra(NODE_ID_KEY)
        } else {
            Timber.e("No node identifier specified")
            finish()
            return
        }
        Timber.v("Using note identifier %s", noteId!!)

        setContentView(R.layout.detail_activity)
        setupSupportActionBar()
        setupFragmentsAndViewPager(savedInstanceState)

        detailViewModel.titleLiveData.observe(this, Observer { this.setTitle(it) })

        detailViewModel.setNote(noteId)

        lifecycle.addObserver(detailController)
        lifecycle.addObserver(SynchronizationTaskLifecycleObserver(synchronizationTask))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.v("onDestroy")
// TODO: LEAK TESTING
//        instrumentation.leakTracing.traceLeakage(this);
    }

    /**
     * Retrieves a [EditorFragment] from a saved instance state or creates a new instance.
     *
     * @param savedInstanceState The [Bundle] to load from.
     * @return A new instance if `savedInstanceState` is `null`, a restored instance from [.getSupportFragmentManager] otherwise.
     */
    private fun getOrCreateEditorFragment(savedInstanceState: Bundle?): EditorFragment {
        return if (savedInstanceState == null) {
            EditorFragment()
        } else {
            supportFragmentManager.getFragment(
                savedInstanceState,
                EDITOR_FRAGMENT_KEY
            ) as EditorFragment
        }
    }

    /**
     * Retrieves a [ViewerFragment] from a saved instance state or creates a new instance.
     *
     * @param savedInstanceState The [Bundle] to load from.
     * @return A new instance if `savedInstanceState` is `null`, a restored instance
     * from [.getSupportFragmentManager] otherwise.
     */
    private fun getOrCreateViewerFragment(savedInstanceState: Bundle?): ViewerFragment {
        return if (savedInstanceState == null) {
            ViewerFragment()
        } else {
            supportFragmentManager.getFragment(
                savedInstanceState,
                VIEWER_FRAGMENT_KEY
            ) as ViewerFragment
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, EDITOR_FRAGMENT_KEY, this.editorFragment)
        supportFragmentManager.putFragment(outState, VIEWER_FRAGMENT_KEY, this.viewerFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            detailController.quit()
            true
        }
        R.id.action_save -> {
            detailController.saveAndQuit()
            true
        }
        R.id.action_delete -> {
            detailController.deleteAndQuit()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        detailController.quit()
    }

    private fun setTitle(title: String) {
        supportActionBar!!.title = title
    }

    private fun setupFragmentsAndViewPager(savedInstanceState: Bundle?) {
        editorFragment = getOrCreateEditorFragment(savedInstanceState)
        viewerFragment = getOrCreateViewerFragment(savedInstanceState)
        viewPager = findViewById<ViewPager>(R.id.detail_view_pager).apply {
            val pagerAdapter = DetailPagerAdapter(
                supportFragmentManager,
                editorFragment,
                viewerFragment
            )
            adapter = pagerAdapter
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    val currentFragment = pagerAdapter.getItem(position)
                    if (currentFragment is OnPageSelectedListener) {
                        currentFragment.onPageSelected()
                    }
                }
            })
        }
    }

    private fun setupSupportActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    internal fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun supportFragmentInjector() = fragmentInjector
}
