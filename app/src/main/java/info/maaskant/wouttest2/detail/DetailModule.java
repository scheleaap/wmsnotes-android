package info.maaskant.wouttest2.detail;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.maaskant.wouttest2.data.NotebookStore;

@Module
public class DetailModule {

    @Provides
    @Singleton
    public DetailViewModel provideRepositoriesViewModel(NotebookStore notebookStore) {
        return new DetailViewModel(notebookStore);
    }

}
