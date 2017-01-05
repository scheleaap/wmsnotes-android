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
        nodeListViewBinder = new NodeListView.ViewBinder(
                (NodeListView) view.findViewById(R.id.node_list_view),
                navigationViewModel);
        navigationViewModel.subscribeToDataStore();
    }


    @Override
    public void onResume() {
        super.onResume();
        nodeListViewBinder.bind();

        navigationViewModel.navigateTo("/storage/emulated/0/");
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
