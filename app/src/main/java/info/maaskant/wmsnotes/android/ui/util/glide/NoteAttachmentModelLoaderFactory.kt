package info.maaskant.wmsnotes.android.ui.util.glide

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.note.Note
import java.nio.ByteBuffer

class NoteAttachmentModelLoaderFactory(
    private val noteRepository: AggregateRepository<Note>
) : ModelLoaderFactory<NoteAttachmentModel, ByteBuffer> {
    override fun build(unused: MultiModelLoaderFactory): ModelLoader<NoteAttachmentModel, ByteBuffer> {
        return NoteAttachmentModelLoader(noteRepository)
    }

    override fun teardown() {
        // Do nothing.
    }
}
