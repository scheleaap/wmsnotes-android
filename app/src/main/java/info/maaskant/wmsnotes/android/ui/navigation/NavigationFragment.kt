package info.maaskant.wmsnotes.android.ui.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import javax.inject.Inject

class NavigationFragment : Fragment() {

    companion object {
        fun newInstance() = NavigationFragment()
    }

    @Inject
    lateinit var viewModel: NavigationViewModel

    private lateinit var nodeListAdapter: NodeListAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var recyclerView: RecyclerView

//    private var recyclerViewScrollEventObservable: Observable<RecyclerViewScrollEvent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nodeListAdapter = NodeListAdapter(emptyList())
        linearLayoutManager = LinearLayoutManager(context)
        return inflater.inflate(R.layout.navigation_fragment, container, false).apply {
            recyclerView = findViewById<RecyclerView>(R.id.node_list_view).apply {
                setHasFixedSize(true)
                layoutManager = linearLayoutManager
                adapter = nodeListAdapter;
            }
//        recyclerViewScrollEventObservable = RxRecyclerView.scrollEvents(recyclerView)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(NavigationViewModel::class.java)
        viewModel.getNotes().observe(this, Observer {
            nodeListAdapter.set(it)
        })
        nodeListAdapter.setOnClickListener(View.OnClickListener {clickedView->
            val itemPosition = recyclerView.getChildAdapterPosition(clickedView)
            val node = nodeListAdapter.getItem(itemPosition)
            viewModel.navigateTo(node)
        })
    }


}
