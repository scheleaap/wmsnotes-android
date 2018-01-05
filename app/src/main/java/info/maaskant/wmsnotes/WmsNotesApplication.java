package info.maaskant.wmsnotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import info.maaskant.wmsnotes.di.Graph;
import info.maaskant.wmsnotes.navigation.NavigationViewModel;
import info.maaskant.wmsnotes.settings.SettingsFragment;
import info.maaskant.wmsnotes.utils.ApplicationInstrumentation;
import timber.log.Timber;

public class WmsNotesApplication extends Application {

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
        String content = "# Kruidkoek\n" + "\n" + "Key | Value\n" + "---|---\n"
                + "Nederland | Amsterdam\n" + "Duitsland | Berlijn\n" + "Frankrijk | Parijs\n"
                + "\n" + "http://www.google.com/\n" + "\n" + "\uD83D\uDE42 \n" + "\n"
                + "\uD83C\uDDF3\uD83C\uDDF5 \n" + "\n" + "Regel\n" + "\n" + "Regel\n" + "\n"
                + "Regel\n" + "\n" + "Regel\n" + "\n" + "Regel\n" + "\n" + "Regel\n" + "\n"
                + "Regel\n" + "\n" + "Regel\n";
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
