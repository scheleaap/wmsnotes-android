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

import com.google.common.base.MoreObjects;

import android.support.annotation.NonNull;

public class FolderNode extends Node<FolderNode> {

    public FolderNode(String id, @NonNull final String name) {
        super(id, name);
    }

    public FolderNode(@NonNull final FolderNode other) {
        this(other.getId(), other.getName());
    }

    @NonNull
    public static FolderNode none() {
        return new FolderNode(null, null);
    }

    @NonNull
    @Override
    protected Class<FolderNode> getTypeParameterClass() {
        return FolderNode.class;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}