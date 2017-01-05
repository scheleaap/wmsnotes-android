package info.maaskant.wouttest2.di;

import dagger.Module;
import dagger.Provides;
import info.maaskant.wouttest2.data.DataFunctions;
import info.maaskant.wouttest2.navigation.NavigationViewModel;

@Module
public class ViewModelModule {

    @Provides
    public NavigationViewModel provideRepositoriesViewModel(DataFunctions.GetChildNodes getChildNodes) {
        return new NavigationViewModel(getChildNodes);
    }

}
