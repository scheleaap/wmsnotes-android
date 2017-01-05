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
package info.maaskant.wouttest2.pojo;

import static io.reark.reark.utils.Preconditions.get;

import java.lang.reflect.Field;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

import android.support.annotation.NonNull;

import io.reark.reark.pojo.OverwritablePojo;
import io.reark.reark.utils.Log;

public class Node extends OverwritablePojo<Node> {

    private static final String TAG = Node.class.getSimpleName();

    private final String id;

    @NonNull
    private String name;

    // @SerializedName("stargazers_count")
    // private int stargazersCount;
    //
    // @SerializedName("forks_count")
    // private int forksCount;

    public Node(String id, @NonNull final String name
    // ,
    // int stargazersCount,
    // int forksCount
    ) {
        this.id = id;
        this.name = get(name);
        // this.stargazersCount = stargazersCount;
        // this.forksCount = forksCount;
    }

    public Node(@NonNull final Node other) {
        this(other.getId(), other.getName()
        // ,
        // other.getStargazersCount(),
        // other.getForksCount()
        );
    }

    @NonNull
    public static Node none() {
        return new Node(null, null
        // , -1, -1
        );
    }

    public boolean isSome() {
        return id != null;
    }

    @NonNull
    @Override
    protected Class<Node> getTypeParameterClass() {
        return Node.class;
    }

//    @Override
//    protected boolean isEmpty(@NonNull final Field field,
//            @NonNull final OverwritablePojo<Node> pojo) {
//        try {
//            if (field.get(pojo) instanceof GitHubOwner) {
//                return false;
//            }
//        } catch (IllegalAccessException e) {
//            Log.e(TAG, "Failed get at " + field.getName(), e);
//        }
//
//        return super.isEmpty(field, pojo);
//    }

    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

//    public int getStargazersCount() {
//        return stargazersCount;
//    }

//    public int getForksCount() {
//        return forksCount;
//    }

    @Override
    public String toString() {
        return "Node{" + "id=" + id + ", name='" + name + '\'' +
        // ", stargazersCount=" + stargazersCount +
        // ", forksCount=" + forksCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Node node = (Node) o;
        return Objects.equal(id, node.id) &&
                Objects.equal(name, node.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name);
    }
}
