package info.maaskant.wmsnotes.android.ui.util.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.note.Note

import java.nio.ByteBuffer

class NoteAttachmentDataFetcher(
    private val noteRepository: AggregateRepository<Note>,
    private val aggId: String,
    private val revision: Int,
    private val attachmentName: String
) : DataFetcher<ByteBuffer> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ByteBuffer>) {
        try {
            val note: Note = noteRepository.get(aggId, revision)
            val attachmentData = note.attachments[attachmentName]
            callback.onDataReady(ByteBuffer.wrap(attachmentData))
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {
        // Intentionally empty only because we're not opening an InputStream or another I/O resource!
    }

    override fun cancel() {}

    override fun getDataClass(): Class<ByteBuffer> =
        ByteBuffer::class.java

    override fun getDataSource(): DataSource =
        DataSource.LOCAL
}
