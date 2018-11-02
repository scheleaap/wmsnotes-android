package info.maaskant.wmsnotes.android.client.indexing

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import info.maaskant.wmsnotes.utilities.serialization.KryoSerializerTest

internal class KryoFolderIndexStateSerializerTest : KryoSerializerTest<FolderIndexState>() {
    private val noteId = "note"

    override val items: List<FolderIndexState> = listOf(
        FolderIndexState(),
        FolderIndexState().addNode(Note("note", "Title"), 1),
        FolderIndexState().addNode(Note("note", "Title"), 1).removeNode("note", 2),
        FolderIndexState().addNode(Folder("folder", "Title"), 2),
        FolderIndexState().addNode(Folder("folder", "Title"), 1).removeNode("folder", 2)
    )

    override fun createInstance(kryoPool: Pool<Kryo>) = KryoFolderIndexStateSerializer(kryoPool)
}
