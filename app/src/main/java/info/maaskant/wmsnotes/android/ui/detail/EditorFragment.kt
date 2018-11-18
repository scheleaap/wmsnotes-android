package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import javax.inject.Inject

class EditorFragment : Fragment() {

    @Inject
    internal var detailViewModel: DetailViewModel? = null

    //    @Inject
    //    ApplicationInstrumentation instrumentation;

    //    private EditorView.ViewBinder editorViewBinder;

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.editor_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        editorViewBinder = new EditorView.ViewBinder(
        //                (EditorView) view.findViewById(R.id.editor_view), detailViewModel);
        //        detailViewModel.subscribeToDataStore();
    }

    override fun onDestroy() {
        super.onDestroy()
        //        instrumentation.getLeakTracing().traceLeakage(this);
    }

}
