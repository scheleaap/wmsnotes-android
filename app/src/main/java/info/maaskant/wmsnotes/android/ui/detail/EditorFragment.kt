package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
import timber.log.Timber
import javax.inject.Inject

class EditorFragment : Fragment(), OnPageSelectedListener {
    @Inject
    lateinit var detailViewModel: DetailViewModel

    @Inject
    lateinit var instrumentation: ApplicationInstrumentation

    private lateinit var editText: EditText

    private var listenForChanges: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.editor_fragment, container, false).apply {
            editText = findViewById<EditText>(R.id.editor_text)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (listenForChanges) {
                    Timber.v("EditText changed: %s", s)
                    detailViewModel.setTextFromUser(editText.text.toString())
                } else {
                    Timber.v("Ignoring EditText change")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        detailViewModel.textUpdatesLiveData.observe(this, Observer {
            if (it.source != TextUpdate.Source.USER) {
                Timber.v("Updating EditText: %s", it.text)
                listenForChanges = false
                editText.setText(it.text)
                listenForChanges = true
            } else {
                Timber.v("EditText not updated")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
// TODO: LEAK TESTING
//        instrumentation.leakTracing.traceLeakage(this);
    }

    /**
     * Called if the page associated with the listener is selected.
     */
    override fun onPageSelected() {
        editText.requestFocus()
    }

}
