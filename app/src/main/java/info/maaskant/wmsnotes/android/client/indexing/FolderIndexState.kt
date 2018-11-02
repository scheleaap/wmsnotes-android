package info.maaskant.wmsnotes.android.client.indexing

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Registration
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.util.Pool
import info.maaskant.wmsnotes.utilities.serialization.KryoSerializer
import info.maaskant.wmsnotes.utilities.serialization.readMap
import info.maaskant.wmsnotes.utilities.serialization.writeMap
import javax.inject.Inject

data class FolderIndexState internal constructor(
    val lastEventId: Int = 0,
    val nodesByFolder: Map<Folder, Map<String, Node>> = emptyMap()
) {
    fun getNodes(folder: Folder): List<Node> {
        return nodesByFolder[folder]?.values?.toList() ?: emptyList()
    }

    fun addNode(node: Node, eventId: Int): FolderIndexState {
        val folder = FolderIndex.rootNode
        val nodesInFolder: Map<String, Node> = nodesByFolder.getOrElse(folder) { emptyMap() }
        val newNotes = nodesByFolder + (folder to (nodesInFolder + (node.nodeId to node)))
        return copy(lastEventId = eventId, nodesByFolder = newNotes)
    }

    fun removeNode(nodeId: String, eventId: Int): FolderIndexState {
        val folder = FolderIndex.rootNode
        val nodesInFolder: Map<String, Node> = nodesByFolder.getOrElse(folder) { emptyMap() }
        val newNotes = nodesByFolder + (folder to (nodesInFolder - nodeId))
        return copy(lastEventId = eventId, nodesByFolder = newNotes)
    }
}

class KryoFolderIndexStateSerializer @Inject constructor(kryoPool: Pool<Kryo>) : KryoSerializer<FolderIndexState>(
    kryoPool,
    Registration(FolderIndexState::class.java, KryoFolderIndexStateSerializer(), 111),
    Registration(Note::class.java, KryoNoteSerializer(), 112),
    Registration(Folder::class.java, KryoFolderSerializer(), 113)
) {

    private class KryoFolderIndexStateSerializer : Serializer<FolderIndexState>() {
        override fun write(kryo: Kryo, output: Output, it: FolderIndexState) {
            output.writeInt(it.lastEventId)
            output.writeMap(it.nodesByFolder) { folder, nodes ->
                kryo.writeObject(output, folder)
                output.writeMap(nodes) { _, node ->
                    kryo.writeClassAndObject(output, node)
                }
            }
        }

        override fun read(kryo: Kryo, input: Input, clazz: Class<out FolderIndexState>): FolderIndexState {
            val lastEventId = input.readInt()
            val nodesByFolder = input.readMap {
                val folder = kryo.readObject(input, Folder::class.java)
                val nodes = input.readMap {
                    val node = kryo.readClassAndObject(input) as Node
                    node.nodeId to node
                }
                folder to nodes
            }
            return FolderIndexState(lastEventId = lastEventId, nodesByFolder = nodesByFolder)
        }
    }

    private class KryoNoteSerializer : Serializer<Note>() {
        override fun write(kryo: Kryo, output: Output, it: Note) {
            output.writeString(it.nodeId)
            output.writeString(it.title)
        }

        override fun read(kryo: Kryo, input: Input, clazz: Class<out Note>): Note {
            val nodeId = input.readString()
            val title = input.readString()
            return Note(nodeId = nodeId, title = title)
        }
    }

    private class KryoFolderSerializer : Serializer<Folder>() {
        override fun write(kryo: Kryo, output: Output, it: Folder) {
            output.writeString(it.nodeId)
            output.writeString(it.title)
        }

        override fun read(kryo: Kryo, input: Input, clazz: Class<out Folder>): Folder {
            val nodeId = input.readString()
            val title = input.readString()
            return Folder(nodeId = nodeId, title = title)
        }
    }
}
