package info.maaskant.wouttest2.navigation;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import info.maaskant.wouttest2.R;
import info.maaskant.wouttest2.WoutTest2App;
import info.maaskant.wouttest2.utils.ApplicationInstrumentation;
import timber.log.Timber;

/**
 * Displays a list of nodes using a {@link NodeListView}.
 */
public class NavigationFragment extends Fragment {

    @Inject
    NavigationViewModel navigationViewModel;

    @Inject
    ApplicationInstrumentation instrumentation;

    private NodeListView.ViewBinder nodeListViewBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate (hash: %s, savedInstanceState: %s)", System.identityHashCode(this), savedInstanceState);

        ((WoutTest2App) getActivity().getApplication()).getGraph().inject(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.node_list_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.v("onViewCreated");
        nodeListViewBinder = new NodeListView.ViewBinder(
                (NodeListView) view.findViewById(R.id.node_list_view),
                navigationViewModel);
//        navigationViewModel.subscribeToDataStore();
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.v("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.v("onResume");
        nodeListViewBinder.bind();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.v("onPause");
        nodeListViewBinder.unbind();
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.v("onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.v("onDestroyView");
//        navigationViewModel.unsubscribeFromDataStore();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.v("onDestroy");
        navigationViewModel.dispose();
        instrumentation.getLeakTracing().traceLeakage(this);
    }

    /**
     * Helps finishing the {@link android.app.Activity} hosting this fragment.
     *
     * @return A boolean indicating whether the back press was consumed.
     */
    public boolean onBackPressed() {
        return this.navigationViewModel.navigateBack();
    }

}
