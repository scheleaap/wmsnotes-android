package info.maaskant.wmsnotes.android.client.indexing

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.util.Pool
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.android.app.OtherModule
import info.maaskant.wmsnotes.client.indexing.DefaultNodeSortingStrategy
import info.maaskant.wmsnotes.client.indexing.KryoTreeIndexStateSerializer
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.client.indexing.TreeIndexState
import info.maaskant.wmsnotes.model.eventstore.EventStore
import info.maaskant.wmsnotes.utilities.persistence.FileStateRepository
import info.maaskant.wmsnotes.utilities.persistence.StateRepository
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class IndexingModule {
    @Provides
    @Singleton
    fun treeIndexStateRepository(@OtherModule.AppDirectory appDirectory: File, kryoPool: Pool<Kryo>): StateRepository<TreeIndexState> =
        FileStateRepository(
            serializer = KryoTreeIndexStateSerializer(kryoPool),
            file = appDirectory.resolve("cache").resolve("tree_index"),
            scheduler = Schedulers.io(),
            timeout = 1,
            unit = TimeUnit.SECONDS
        )

    @Provides
    @Singleton
    fun treeIndex(eventStore: EventStore, stateRepository: StateRepository<TreeIndexState>): TreeIndex {
        return TreeIndex(
            eventStore,
            DefaultNodeSortingStrategy(),
            stateRepository.load(),
            Schedulers.io()
        ).apply {
            stateRepository.connect(this)
        }
    }
}
