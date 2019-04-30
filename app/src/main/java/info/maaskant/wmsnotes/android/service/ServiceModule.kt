package info.maaskant.wmsnotes.android.service

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.model.folder.FolderCommandExecutor
import info.maaskant.wmsnotes.model.note.NoteCommandExecutor
import info.maaskant.wmsnotes.model.note.policy.NoteTitlePolicy
import info.maaskant.wmsnotes.utilities.ApplicationService
import javax.inject.Singleton

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    internal abstract fun applicationServiceManager(): ApplicationServiceManager
}
