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
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.ui.detail.DetailViewModel.Update.Origin
import info.maaskant.wmsnotes.utilities.logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.sellmair.disposer.disposeBy
import io.sellmair.disposer.onStop
import javax.inject.Inject

class EditorFragment : Fragment(), OnPageSelectedListener {
    private val logger by logger()

    @Inject
    lateinit var detailViewModel: DetailViewModel

    private lateinit var contentField: EditText

    private var listenForChanges: Boolean = true

    private fun focusFieldAndShowKeyboard() {
        if (!contentField.hasFocus()) {
            contentField.requestFocus()
            getSystemService(requireContext(), InputMethodManager::class.java)?.showSoftInput(
                contentField,
                SHOW_IMPLICIT
            )
        }
    }

    private fun hideKeyboard() {
        if (contentField.hasFocus()) {
            getSystemService(
                requireContext(),
                InputMethodManager::class.java
            )?.hideSoftInputFromWindow(contentField.windowToken, 0)
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
            contentField = findViewById<EditText>(R.id.editor_content).apply {
                isSaveEnabled = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (listenForChanges) {
                    logger.trace("Content EditText changed: {}", s)
                    detailViewModel.setContentFromUser(contentField.text.toString())
                } else {
                    logger.trace("Ignoring content EditText change")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        detailViewModel.getContentUpdates()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.origin != Origin.VIEW) {
                    logger.trace("Updating content EditText: {}", it.value)
                    listenForChanges = false
                    contentField.setText(it.value)
                    listenForChanges = true
                } else {
                    logger.trace("Content EditText not updated")
                }
            }
            .disposeBy(onStop)
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
