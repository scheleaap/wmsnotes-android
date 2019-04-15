package info.maaskant.wmsnotes.android.app

import android.content.Context
import dagger.Module
import dagger.Provides
import info.maaskant.wmsnotes.R
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class StringsModule {
    @Qualifier
    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    annotation class StringId(
        /** The string resource id.  */
        val value: Int
    )

    @Provides
    @Singleton
    @StringId(R.string.new_note_title)
    fun newNoteTitle(context: Context): String = context.getString(R.string.new_note_title)
}
