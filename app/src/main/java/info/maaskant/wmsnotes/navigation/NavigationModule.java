package info.maaskant.wmsnotes.navigation;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.maaskant.wmsnotes.data.NotebookStore;

@Module
public class NavigationModule {

    @Provides
    @Singleton
    public NavigationViewModel provideRepositoriesViewModel(NotebookStore notebookStore) {
        return new NavigationViewModel(notebookStore);
    }

}
