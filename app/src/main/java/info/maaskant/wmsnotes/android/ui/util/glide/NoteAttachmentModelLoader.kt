package info.maaskant.wmsnotes.android.ui.util.glide

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.note.Note
import java.nio.ByteBuffer

class NoteAttachmentModelLoader(
    private val noteRepository: AggregateRepository<Note>
) : ModelLoader<NoteAttachmentModel, ByteBuffer> {
    override fun buildLoadData(
        model: NoteAttachmentModel,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<ByteBuffer> {
        return ModelLoader.LoadData(
            ObjectKey(model),
            NoteAttachmentDataFetcher(
                noteRepository = noteRepository,
                aggId = model.aggId,
                revision = model.revision,
                attachmentName = model.attachmentName
            )
        )
    }

    override fun handles(model: NoteAttachmentModel): Boolean =
        true
}
