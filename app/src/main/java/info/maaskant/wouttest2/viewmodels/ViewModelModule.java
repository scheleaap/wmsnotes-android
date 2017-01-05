package info.maaskant.wouttest2.viewmodels;

import dagger.Module;
import dagger.Provides;
import info.maaskant.wouttest2.data.DataFunctions;

@Module
public class ViewModelModule {

    @Provides
    public NavigationViewModel provideRepositoriesViewModel(DataFunctions.GetChildNodes getChildNodes) {
        return new NavigationViewModel(getChildNodes);
    }

}
