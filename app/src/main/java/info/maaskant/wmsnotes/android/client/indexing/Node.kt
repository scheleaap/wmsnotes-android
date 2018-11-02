package info.maaskant.wmsnotes.android.client.indexing

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinToString
import java.util.*

sealed class Node(val nodeId: String, val title: String) {
    override fun equals(other: Any?) =
        kotlinEquals(other = other, properties = arrayOf(Node::nodeId, Node::title))

    override fun hashCode() = Objects.hash(nodeId, title)

    override fun toString() = kotlinToString(properties = arrayOf(Node::nodeId, Node::title))

}
class Folder(nodeId: String, title: String) : Node(nodeId, title)
class Note(nodeId: String, title: String) : Node(nodeId, title)
