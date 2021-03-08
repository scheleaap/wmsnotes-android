package info.maaskant.wmsnotes.android.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.utilities.logger
import javax.inject.Inject

@AndroidEntryPoint
class ViewerFragment @Inject constructor() : Fragment() {
    private val logger by logger()

    private val detailViewModel: DetailViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.trace("onCreate (this: {})", System.identityHashCode(this))
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        logger.trace(
            "onCreateView (this: {}, vm: {})",
            System.identityHashCode(this),
            System.identityHashCode(detailViewModel)
        )
        return inflater.inflate(R.layout.viewer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logger.trace("onViewCreated (this: {})", System.identityHashCode(this))
        super.onViewCreated(view, savedInstanceState)
        val textView = view.findViewById<TextView>(R.id.viewer_textview)
        lifecycle.addObserver(Renderer(detailViewModel, requireContext(), textView))
    }
}

