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
package info.maaskant.wouttest2.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import info.maaskant.wouttest2.pojo.Node;
import info.maaskant.wouttest2.pojo.UserSettings;
import io.reark.reark.data.DataStreamNotification;
import rx.Observable;

public class DataFunctions {
    public interface GetUserSettings {
        @NonNull
        Observable<UserSettings> call();
    }

    public interface SetUserSettings {
        void call(@NonNull final UserSettings userSettings);
    }

    public interface GetChildNodes {
        /**
         * Get the child nodes of a node.
         *
         * @param parentNodeId
         *            The identifier of the parent of the children, {@code null} to get the root node.
         * @return An {@link Observable} of {@link DataStreamNotification} of {@link List} of {@link Node}.
         */
        @NonNull
        Observable<DataStreamNotification<List<Node>>> call(@Nullable final String parentNodeId);
    }

}
