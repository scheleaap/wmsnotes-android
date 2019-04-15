package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.instrumentation.ApplicationInstrumentation
import javax.inject.Inject

class ViewerFragment : Fragment() {

    @Inject
    lateinit var detailViewModel: DetailViewModel

    @Inject
    lateinit var instrumentation: ApplicationInstrumentation

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.viewer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView = view.findViewById<TextView>(R.id.viewer_textview)
        lifecycle.addObserver(Renderer(detailViewModel, requireContext(), textView))
    }

    override fun onDestroy() {
        super.onDestroy()
// TODO: LEAK TESTING
//        instrumentation.leakTracing.traceLeakage(this);
    }
}

