package info.maaskant.wmsnotes.android.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import info.maaskant.wmsnotes.R

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var listener: Listener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false).apply {
            findViewById<Button>(R.id.start).setOnClickListener { listener.onStartNavigatingButtonPressed() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is Listener) throw RuntimeException(context.toString() + " must implement ${Listener::class.java.simpleName}")
        listener = context
    }

    interface Listener {
        fun onStartNavigatingButtonPressed()
    }

}
