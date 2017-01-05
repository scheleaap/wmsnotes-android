package info.maaskant.wouttest2;

import android.app.Application;

import javax.inject.Inject;

import info.maaskant.wouttest2.di.Graph;
import info.maaskant.wouttest2.utils.ApplicationInstrumentation;

public class WoutTest2App extends Application {

    private Graph graph;

    @Inject
    ApplicationInstrumentation instrumentation;

    @Override
    public void onCreate() {
        super.onCreate();

        graph = Graph.Initializer.init(this);
        getGraph().inject(this);

        instrumentation.init();
    }

    public Graph getGraph() {
       return graph;
    }
}
