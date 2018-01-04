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
import com.google.common.base.Objects;

import android.support.annotation.NonNull;

import io.reark.reark.pojo.OverwritablePojo;

import static java.util.Objects.requireNonNull;

public abstract class Node<T extends Node<T>> extends OverwritablePojo<T> {

    private final String id;

    @NonNull
    private String name;

    // @SerializedName("stargazers_count")
    // private int stargazersCount;
    //
    // @SerializedName("forks_count")
    // private int forksCount;

    Node(String id, @NonNull final String name
    // ,
    // int stargazersCount,
    // int forksCount
    ) {
        this.id = id;
        this.name = requireNonNull(name);
        // this.stargazersCount = stargazersCount;
        // this.forksCount = forksCount;
    }

    Node(@NonNull final Node other) {
        this(other.getId(), other.getName()
        // ,
        // other.getStargazersCount(),
        // other.getForksCount()
        );
    }

    // public boolean isSome() {
    // return id != null;
    // }

    // @Override
    // protected boolean isEmpty(@NonNull final Field field,
    // @NonNull final OverwritablePojo<Node> pojo) {
    // try {
    // if (field.get(pojo) instanceof GitHubOwner) {
    // return false;
    // }
    // } catch (IllegalAccessException e) {
    // Log.e(TAG, "Failed get at " + field.getName(), e);
    // }
    //
    // return super.isEmpty(field, pojo);
    // }

    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    // public int getStargazersCount() {
    // return stargazersCount;
    // }

    // public int getForksCount() {
    // return forksCount;
    // }

    /**
     * Visit a visitor with this node.
     *
     * @param visitor
     *            The {@link NodeVisitor} to call.
     */
    public abstract void accept(NodeVisitor visitor);

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        Node node = (Node) o;
        return Objects.equal(id, node.id) && Objects.equal(name, node.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name);
    }
}
