package info.maaskant.wouttest2.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.maaskant.wouttest2.data.DataFunctions;
import info.maaskant.wouttest2.navigation.NavigationViewModel;

@Module
public class ViewModelModule {

    @Provides
    @Singleton
    public NavigationViewModel provideRepositoriesViewModel(DataFunctions.GetChildNodes getChildNodes) {
        return new NavigationViewModel(getChildNodes);
    }

}
