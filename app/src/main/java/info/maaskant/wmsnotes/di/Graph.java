/*
 * The MIT License
 *
 * Copyright (c) 2013-2016 reark project contributors
 *
 * https://github.com/reark/reark/graphs/contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.maaskant.wmsnotes.di;

import javax.inject.Singleton;

import android.app.Application;

import dagger.Component;
import info.maaskant.wmsnotes.MainActivity;
import info.maaskant.wmsnotes.detail.DetailActivity;
import info.maaskant.wmsnotes.detail.DetailModule;
import info.maaskant.wmsnotes.detail.EditorFragment;
import info.maaskant.wmsnotes.detail.ViewerFragment;
import info.maaskant.wmsnotes.navigation.NavigationFragment;
import info.maaskant.wmsnotes.WmsNotesApplication;
import info.maaskant.wmsnotes.data.DataModule;
import info.maaskant.wmsnotes.navigation.NavigationModule;
import info.maaskant.wmsnotes.navigation.NavigationViewModel;

@Singleton
@Component(modules = { ApplicationModule.class, DataModule.class, NavigationModule.class,
        DetailModule.class, InstrumentationModule.class })
public interface Graph {

    void inject(DetailActivity detailActivity);

    void inject(EditorFragment editorFragment);

    void inject(MainActivity mainActivity);

    void inject(NavigationFragment navigationFragment);

    void inject(NavigationViewModel navigationViewModel);

    void inject(ViewerFragment viewerFragment);

    void inject(WmsNotesApplication application);

    final class Initializer {

        public static Graph init(Application application) {
            return DaggerGraph.builder().applicationModule(new ApplicationModule(application))
                    .build();
        }
    }
}
