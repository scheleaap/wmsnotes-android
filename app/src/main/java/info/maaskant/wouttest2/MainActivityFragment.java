package info.maaskant.wouttest2;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import info.maaskant.wouttest2.utils.ApplicationInstrumentation;
import info.maaskant.wouttest2.view.NodeListView;
import info.maaskant.wouttest2.viewmodels.NavigationViewModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    @Inject
    NavigationViewModel navigationViewModel;

    @Inject
    ApplicationInstrumentation instrumentation;

    private NodeListView.ViewBinder nodeListViewBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((WoutTest2App) getActivity().getApplication()).getGraph().inject(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.node_list_fragment, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nodeListViewBinder = new NodeListView.ViewBinder(
                (NodeListView) view.findViewById(R.id.node_list_view),
                navigationViewModel);
        navigationViewModel.subscribeToDataStore();
    }


    @Override
    public void onResume() {
        super.onResume();
        nodeListViewBinder.bind();

        navigationViewModel.setCurrentParentNodeId("/storage/emulated/0/");
    }

    @Override
    public void onPause() {
        super.onPause();
        nodeListViewBinder.unbind();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        navigationViewModel.unsubscribeFromDataStore();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        navigationViewModel.dispose();
        instrumentation.getLeakTracing().traceLeakage(this);
    }}
