package info.maaskant.wmsnotes.android.ui.detail

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import info.maaskant.wmsnotes.R
import timber.log.Timber
import javax.inject.Inject

class DetailActivity : AppCompatActivity(), HasSupportFragmentInjector {

    companion object {
        const val NODE_ID_KEY = "nodeId"
        private const val EDITOR_FRAGMENT_KEY = "editorFragment"
        private const val VIEWER_FRAGMENT_KEY = "viewerFragment"
    }

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var detailViewModel: DetailViewModel

//    @Inject
    //    ApplicationInstrumentation instrumentation;

    private var nodeId: String? = null

    private var editorFragment: EditorFragment? = null

    private var viewerFragment: ViewerFragment? = null

    private var saveButton: Button? = null

    private var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v(
            "onCreate (hash: %s, savedInstanceState: %s)", System.identityHashCode(this),
            savedInstanceState
        )

        // TODO Move up
        AndroidInjection.inject(this)

        setContentView(R.layout.activity_detail)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra(NODE_ID_KEY)) {
            nodeId = intent.getStringExtra(NODE_ID_KEY)
        } else {
            Timber.e("No node identifier specified")
            finish()
            return
        }
        Timber.v("Using node identifier " + nodeId!!)

        saveButton = findViewById<View>(R.id.detail_save_button) as Button

        this.editorFragment = getOrCreateEditorFragment(savedInstanceState)
        this.viewerFragment = getOrCreateViewerFragment(savedInstanceState)
        viewPager = findViewById<View>(R.id.detail_view_pager) as ViewPager
        viewPager!!.adapter = DetailPagerAdapter(
            supportFragmentManager,
            this.editorFragment!!, this.viewerFragment!!
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        Timber.v("onDestroyView")
        //        instrumentation.getLeakTracing().traceLeakage(this);
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

    // @Override
    // public void onBackPressed() {
    // if (!navigationFragment.onBackPressed()) {
    // super.onBackPressed();
    // }
    // }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, EDITOR_FRAGMENT_KEY, this.editorFragment!!)
        supportFragmentManager.putFragment(outState, VIEWER_FRAGMENT_KEY, this.viewerFragment!!)
    }

    internal fun setSupportActionBarTitle(title: String) {
        supportActionBar!!.title = title
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
