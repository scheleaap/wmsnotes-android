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
package info.maaskant.wouttest2.model;

import android.support.annotation.NonNull;

public class UserSettings {

    @Deprecated // TODO: Wout: Unused
    private final String currentParentNodeId;

    public UserSettings(String currentParentNodeId) {
        this.currentParentNodeId = currentParentNodeId;
    }

    @NonNull
    public static UserSettings none() {
        return new UserSettings(null);
    }

    public boolean isSome() {
        return currentParentNodeId != null;
    }

    public boolean isNone() {
        return !isSome();
    }

    public String getCurrentParentNodeId() {
        return currentParentNodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSettings that = (UserSettings) o;
        return currentParentNodeId == that.currentParentNodeId;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(currentParentNodeId);
    }

}
