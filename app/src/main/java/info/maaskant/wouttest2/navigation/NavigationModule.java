package info.maaskant.wouttest2.navigation;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.maaskant.wouttest2.data.NotebookStore;
import info.maaskant.wouttest2.navigation.NavigationViewModel;

@Module
public class NavigationModule {

    @Provides
    @Singleton
    public NavigationViewModel provideRepositoriesViewModel(NotebookStore notebookStore) {
        return new NavigationViewModel(notebookStore);
    }

}
