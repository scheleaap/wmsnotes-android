package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.EmergencyBrakeActivityFinisher
import info.maaskant.wmsnotes.android.client.synchronization.EmergencyBrake
import info.maaskant.wmsnotes.android.service.ApplicationServiceManager
import info.maaskant.wmsnotes.android.ui.di.DefaultFragmentFactory
import info.maaskant.wmsnotes.client.synchronization.SynchronizationTask
import info.maaskant.wmsnotes.model.CommandBus
import info.maaskant.wmsnotes.utilities.logger
import javax.inject.Inject

@AndroidEntryPoint
class DetailActivity : AppCompatActivity() {
    companion object {
        const val AGG_ID_KEY = "aggId"
        private const val EDITOR_FRAGMENT_KEY = "editorFragment"
        private const val VIEWER_FRAGMENT_KEY = "viewerFragment"
    }

    private val logger by logger()

    private val detailViewModel: DetailViewModel by viewModels()

    lateinit var detailController: DetailController

    @Inject
    lateinit var commandBus: CommandBus

    @Inject
    lateinit var emergencyBrake: EmergencyBrake

    @Inject
    lateinit var synchronizationTask: SynchronizationTask

    private lateinit var editorFragment: EditorFragment

    private lateinit var viewerFragment: ViewerFragment

    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.trace(
            "onCreate (this: {}, savedInstanceState: {})",
            System.identityHashCode(this),
            savedInstanceState
        )

        DefaultFragmentFactory.setUp(this)
        super.onCreate(savedInstanceState)

        logger.trace("DetailActivity: vm={}", detailViewModel)

        detailController = DetailController(detailViewModel, commandBus, this)

        setContentView(R.layout.detail_activity)
        setupSupportActionBar()
        setupFragmentsAndViewPager(savedInstanceState)

        this.setTitle("")

        val aggId = if (intent.hasExtra(AGG_ID_KEY)) {
            intent.getStringExtra(AGG_ID_KEY)!!
        } else {
            logger.error("No node identifier specified")
            finish()
            return
        }
        logger.trace("Using note identifier {}", aggId)

        detailViewModel.setNote(aggId)
        detailViewModel.restoreState(savedInstanceState)

        lifecycle.addObserver(detailController)
        lifecycle.addObserver(ApplicationServiceManager.ServiceBindingLifecycleObserver(this))
        lifecycle.addObserver(EmergencyBrakeActivityFinisher(emergencyBrake, this))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.trace("onDestroy")
    }

    /**
     * Retrieves a [EditorFragment] from a saved instance state or creates a new instance.
     *
     * @param savedInstanceState The [Bundle] to load from.
     * @return A new instance if `savedInstanceState` is `null`, a restored instance from [.getSupportFragmentManager] otherwise.
     */
    private fun getOrCreateEditorFragment(savedInstanceState: Bundle?): EditorFragment {
        return if (savedInstanceState == null) {
            supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                EditorFragment::class.java.name
            ) as EditorFragment
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
            supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                ViewerFragment::class.java.name
            ) as ViewerFragment
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
        outState.putAll(detailViewModel.getStateToSave())
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
                    for (i in 0 until pagerAdapter.count) {
                        val fragment = pagerAdapter.getItem(i)
                        if (fragment is OnPageSelectedListener) {
                            if (i == position) {
                                fragment.onPageSelected()
                            } else {
                                fragment.onPageDeselected()
                            }
                        }
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
}
