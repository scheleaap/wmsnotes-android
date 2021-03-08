package info.maaskant.wmsnotes.android.ui.util.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import info.maaskant.wmsnotes.model.aggregaterepository.AggregateRepository
import info.maaskant.wmsnotes.model.note.Note
import java.nio.ByteBuffer

// Source: https://github.com/bumptech/glide/issues/2002#issuecomment-683222928
@GlideModule
class GlideModule : AppGlideModule() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface GlideModuleEntryPoint {
        fun getNoteRepository(): AggregateRepository<Note>
    }

    override fun isManifestParsingEnabled(): Boolean = false

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val appContext = context.applicationContext
        val entryPoint: GlideModuleEntryPoint =
            EntryPointAccessors.fromApplication(appContext, GlideModuleEntryPoint::class.java)

        registry.append(
            NoteAttachmentModel::class.java,
            ByteBuffer::class.java,
            NoteAttachmentModelLoaderFactory(entryPoint.getNoteRepository())
        )
    }
}
