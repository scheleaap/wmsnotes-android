package info.maaskant.wmsnotes.android.client.indexing

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.client.indexing.KryoNoteIndexStateSerializer
import info.maaskant.wmsnotes.client.indexing.NoteIndex
import info.maaskant.wmsnotes.client.indexing.NoteIndexState
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.utilities.persistence.FileStateRepository
import info.maaskant.wmsnotes.utilities.persistence.StateRepository
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class IndexingModule {

    @Singleton
    @Provides
    fun folderIndexStateRepository(@OtherModule.AppDirectory appDirectory: File, kryoPool: Pool<Kryo>): StateRepository<FolderIndexState> =
        FileStateRepository(
            serializer = KryoFolderIndexStateSerializer(kryoPool),
            file = appDirectory.resolve("cache").resolve("folder_index"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Singleton
    @Provides
    fun folderIndex(eventStore: EventStore, stateRepository: StateRepository<FolderIndexState>): FolderIndex {
        return FolderIndex(
            eventStore,
            stateRepository.load(),
            Schedulers.io()
        ).apply {
            stateRepository.connect(this)
        }
    }

    @Singleton
    @Provides
    fun noteIndexStateRepository(@OtherModule.AppDirectory appDirectory: File, kryoPool: Pool<Kryo>): StateRepository<NoteIndexState> =
        FileStateRepository(
            serializer = KryoNoteIndexStateSerializer(kryoPool),
            file = appDirectory.resolve("cache").resolve("note_index"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Singleton
    @Provides
    fun noteIndex(eventStore: EventStore, stateRepository: StateRepository<NoteIndexState>): NoteIndex {
        return NoteIndex(
            eventStore,
            stateRepository.load(),
            Schedulers.io()
        ).apply {
            stateRepository.connect(this)
        }
    }

}
