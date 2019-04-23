package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
import info.maaskant.wmsnotes.android.ui.detail.DetailViewModel.Update.Source
import timber.log.Timber
import javax.inject.Inject

class EditorFragment : Fragment(), OnPageSelectedListener {
    @Inject
    lateinit var detailViewModel: DetailViewModel

    @Inject
    lateinit var instrumentation: ApplicationInstrumentation

    private lateinit var contentField: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var titleField: EditText

    private var listenForChanges: Boolean = true

    private fun focusFieldAndShowKeyboard() {
        if (!(titleField.hasFocus() || contentField.hasFocus())) {
            val field = if (contentField.text.isEmpty()) {
                titleField.setSelection(titleField.text.length)
                titleField
            } else {
                contentField
            }
            field.requestFocus()
            getSystemService(requireContext(), InputMethodManager::class.java)?.showSoftInput(field, SHOW_IMPLICIT)
            scrollView.scrollTo(0, titleField.top)
        }
    }

    private fun hideKeyboard() {
        when {
            titleField.hasFocus() -> titleField
            contentField.hasFocus() -> contentField
            else -> null
        }?.let { field ->
            getSystemService(
                requireContext(),
                InputMethodManager::class.java
            )?.hideSoftInputFromWindow(field.windowToken, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.editor_fragment, container, false).apply {
            scrollView = findViewById(R.id.editor_view)
            contentField = findViewById(R.id.editor_content)
            titleField = findViewById(R.id.editor_title)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (listenForChanges) {
                    Timber.v("Content EditText changed: %s", s)
                    detailViewModel.setContentFromUser(contentField.text.toString())
                } else {
                    Timber.v("Ignoring content EditText change")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        titleField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (listenForChanges) {
                    Timber.v("Title EditText changed: %s", s)
                    detailViewModel.setTitleFromUser(titleField.text.toString())
                } else {
                    Timber.v("Ignoring title EditText change")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        detailViewModel.contentUpdatesLiveData.observe(this, Observer {
            if (it.source != Source.USER) {
                Timber.v("Updating content EditText: %s", it.value)
                listenForChanges = false
                contentField.setText(it.value)
                listenForChanges = true
            } else {
                Timber.v("Content EditText not updated")
            }
        })
        detailViewModel.titleUpdatesLiveData.observe(this, Observer {
            if (it.source != Source.USER) {
                Timber.v("Updating title EditText: %s", it.value)
                listenForChanges = false
                titleField.setText(it.value)
                listenForChanges = true
            } else {
                Timber.v("Title EditText not updated")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
// TODO: LEAK TESTING
//        instrumentation.leakTracing.traceLeakage(this);
    }

    /**
     * Called if the page associated with the listener is deselected.
     */
    override fun onPageDeselected() {
        hideKeyboard()
    }

    /**
     * Called if the page associated with the listener is selected.
     */
    override fun onPageSelected() {
        focusFieldAndShowKeyboard()
    }
}
