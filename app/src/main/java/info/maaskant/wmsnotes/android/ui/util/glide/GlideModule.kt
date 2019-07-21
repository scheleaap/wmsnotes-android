package info.maaskant.wmsnotes.android.ui.util.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import info.maaskant.wmsnotes.android.app.App
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.note.Note
import java.nio.ByteBuffer
import javax.inject.Inject

@GlideModule
class GlideModule : AppGlideModule() {
    @Inject
    lateinit var noteRepository: AggregateRepository<Note>

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // This solutions does not feel very clean.
        // Source: https://github.com/bumptech/glide/issues/2002#issuecomment-312521168
        (context.applicationContext as App).component.inject(this)
        registry.append(
            NoteAttachmentModel::class.java,
            ByteBuffer::class.java,
            NoteAttachmentModelLoaderFactory(noteRepository)
        )
    }
}
