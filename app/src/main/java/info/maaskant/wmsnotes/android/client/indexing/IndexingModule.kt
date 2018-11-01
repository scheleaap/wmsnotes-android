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
    fun noteIndexStateRepository(@OtherModule.AppDirectory appDirectory: File, kryoPool: Pool<Kryo>): StateRepository<NoteIndexState> =
        FileStateRepository<NoteIndexState>(
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

    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class IndexDatabase

}
