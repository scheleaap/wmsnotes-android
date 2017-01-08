package info.maaskant.wouttest2;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;

import info.maaskant.wouttest2.di.Graph;
import info.maaskant.wouttest2.navigation.NavigationViewModel;
import info.maaskant.wouttest2.settings.SettingsFragment;
import info.maaskant.wouttest2.utils.ApplicationInstrumentation;
import timber.log.Timber;

public class WoutTest2App extends Application {

    @Inject
     ApplicationInstrumentation instrumentation;

    @Inject
     NavigationViewModel navigationViewModel;

    private Graph graph;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        graph = Graph.Initializer.init(this);
        getGraph().inject(this);

        instrumentation.init();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String notebookPath = sharedPreferences.getString(SettingsFragment.NOTEBOOK_PATH_KEY,
                getResources().getString(R.string.pref_default_notebook_path));
        navigationViewModel.navigateForward(notebookPath);

        writeMarkdownFile();
    }

    private void writeMarkdownFile() {
        // Write Markdown file
        String fileName = "test.md";
        String content = "# Kruidkoek\n" + "\n" + "a|b\n" + "---|---\n" + "c|d\n" + "\n"
                + "http://www.google.com/";
        try {
            File path = new File("/storage/emulated/0/Download", fileName);
            Timber.i("Writing test data to %s", path);
            Files.write(content, path, Charsets.UTF_8);
        } catch (FileNotFoundException e) {
            Timber.e("Could not write file", e);
            return;
        } catch (IOException e) {
            Timber.e("Could not write file", e);
            return;
        }
    }

    public Graph getGraph() {
        return graph;
    }
}
