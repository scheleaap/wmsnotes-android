package info.maaskant.wouttest2.detail;

import javax.inject.Inject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.maaskant.wouttest2.R;
import info.maaskant.wouttest2.WoutTest2App;
import info.maaskant.wouttest2.utils.ApplicationInstrumentation;
import timber.log.Timber;

public class ViewerFragment extends Fragment {

    @Inject
    DetailViewModel detailViewModel;

    @Inject
    ApplicationInstrumentation instrumentation;

    private ViewerView.ViewBinder viewerViewBinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((WoutTest2App) getActivity().getApplication()).getGraph().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewer_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewerViewBinder = new ViewerView.ViewBinder(
                (ViewerView) view.findViewById(R.id.viewer_fragment), detailViewModel);
        detailViewModel.subscribeToDataStore();
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

        viewerViewBinder.bind();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.v("onPause");
        viewerViewBinder.unbind();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.v("onDestroy");
        detailViewModel.dispose();
        instrumentation.getLeakTracing().traceLeakage(this);
    }

}
