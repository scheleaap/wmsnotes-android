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
import javax.inject.Inject

class EditorFragment : Fragment() {

    @Inject
    lateinit var detailViewModel: DetailViewModel

    //    @Inject
    //    ApplicationInstrumentation instrumentation;

    private lateinit var editText: EditText

//    private var listenForChanges: Boolean = true

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
//                if (listenForChanges) {
                    detailViewModel.setText(editText.text.toString())
//                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        detailViewModel.textUpdatesForEditorLiveData.observe(this, Observer {
//            listenForChanges = false
            editText.setText(it)
//            listenForChanges = true
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        //        instrumentation.getLeakTracing().traceLeakage(this);
    }

}
