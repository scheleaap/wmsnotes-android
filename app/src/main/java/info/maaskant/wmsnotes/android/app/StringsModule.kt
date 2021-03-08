package info.maaskant.wmsnotes.android.app

import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.maaskant.wmsnotes.R
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
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
    fun string001(resources: Resources): String = resources.getString(R.string.new_note_title)

    @Provides
    @Singleton
    @StringId(R.string.create_folder_dialog_error_title_must_not_be_empty)
    fun string002(resources: Resources): String =
        resources.getString(R.string.create_folder_dialog_error_title_must_not_be_empty)

    @Provides
    @Singleton
    @StringId(R.string.create_folder_dialog_error_title_must_not_contain_slash)
    fun string003(resources: Resources): String =
        resources.getString(R.string.create_folder_dialog_error_title_must_not_contain_slash)
}
