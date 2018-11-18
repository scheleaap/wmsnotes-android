package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import javax.inject.Inject

class ViewerFragment : Fragment() {

    @Inject
    lateinit var detailViewModel: DetailViewModel

    //    @Inject
    //    ApplicationInstrumentation instrumentation;

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
//        viewerViewBinder = ViewerView.ViewBinder(
//            view.findViewById<View>(R.id.viewer_view) as ViewerView, detailViewModel
//        )
        //        detailViewModel.subscribeToDataStore();
    }

    override fun onDestroy() {
        super.onDestroy()
        //        instrumentation.getLeakTracing().traceLeakage(this);
    }

}
