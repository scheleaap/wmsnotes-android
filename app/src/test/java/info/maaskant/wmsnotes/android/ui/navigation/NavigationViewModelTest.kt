package info.maaskant.wmsnotes.android.ui.navigation

import android.os.Bundle
import info.maaskant.wmsnotes.android.ui.navigation.NavigationViewModel.FolderTitleValidity.Invalid
import info.maaskant.wmsnotes.android.ui.navigation.NavigationViewModel.FolderTitleValidity.Valid
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.model.CommandBus
import info.maaskant.wmsnotes.model.Path
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.shouldHaveThrown
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import info.maaskant.wmsnotes.android.ui.navigation.Folder as NIFolder
import info.maaskant.wmsnotes.android.ui.navigation.Note as NINote
import info.maaskant.wmsnotes.client.indexing.Folder as TIFolder
import info.maaskant.wmsnotes.client.indexing.Note as TINote

internal class NavigationViewModelTest {
    private val path = Path()
    private val aggId1 = "agg-1"
    private val aggId2 = "agg-2"
    private val title = "Title"

    private val commandBus: CommandBus = mockk()

    private val treeIndex: TreeIndex = mockk()

    @BeforeEach
    fun init() {
        clearMocks(
            commandBus,
            treeIndex
        )

        every { treeIndex.getEvents(any()) }.returns(Observable.empty())
        every { treeIndex.getNodes(any()) }.returns(Observable.empty())
    }

    @Test
    fun `initial values`() {
        // Given
        val model = createInstance()
        val observer: TestObserver<ImmutableStack<Path>> = model.getStack().test()

        // When

        // Then
        assertThat(observer.values().toList()).isEqualTo(
            listOf(
                ImmutableStack.from(Path())
            )
        )
    }

    @Test
    fun `navigate to child`() {
        // Given
        val model = createInstance()
        val observer: TestObserver<ImmutableStack<Path>> = model.getStack().test()

        // When
        model.navigateTo(Path("el1"))

        // Then
        assertThat(observer.values().toList()).isEqualTo(
            listOf(
                ImmutableStack.from(Path()),
                ImmutableStack.from(Path(), Path("el1"))
            )
        )
    }

    @Test
    fun `navigate to a path that is not a child`() {
        // Given
        val model = createInstance()
        model.navigateTo(Path("el1"))
        val observer: TestObserver<ImmutableStack<Path>> = model.getStack().test()

        // When / then
        try {
            model.navigateTo(Path("el2"))
            shouldHaveThrown<Unit>(IllegalArgumentException::class.java)
        } catch (e: IllegalArgumentException) {
        }
        assertThat(observer.values().toList()).isEqualTo(
            listOf(
                ImmutableStack.from(Path(), Path("el1"))
            )
        )
    }

    @Test
    fun `navigate up`() {
        // Given
        val model = createInstance()
        model.navigateTo(Path("el1"))
        model.navigateTo(Path("el1", "el2"))
        val observer: TestObserver<ImmutableStack<Path>> = model.getStack().test()

        // When
        val result = model.navigateUp()

        // Then
        assertThat(result).isEqualTo(true)
        assertThat(observer.values().toList()).isEqualTo(
            listOf(
                ImmutableStack.from(Path(), Path("el1"), Path("el1", "el2")),
                ImmutableStack.from(Path(), Path("el1"))
            )
        )
    }

    @Test
    fun `navigate up, in root folder`() {
        // Given
        val model = createInstance()
        val observer: TestObserver<ImmutableStack<Path>> = model.getStack().test()

        // When
        val result = model.navigateUp()

        // Then
        assertThat(result).isEqualTo(false)
        assertThat(observer.values().toList()).isEqualTo(
            listOf(
                ImmutableStack.from(Path())
            )
        )
    }

    @Test
    fun `restore state`() {
        // Given
        val bundle: Bundle = mockk()
        every { bundle.containsKey("currentPath") }.returns(true)
        every { bundle.getString("currentPath") }.returns(Path("el1", "el2").toString())
        val model = createInstance()
        val observer: TestObserver<ImmutableStack<Path>> = model.getStack().test()

        // When
        model.restoreState(bundle)

        // Then
        assertThat(observer.values().toList()).isEqualTo(
            listOf(
                ImmutableStack.from(Path()),
                ImmutableStack.from(Path(), Path("el1"), Path("el1", "el2"))
            )
        )
    }

    @Test
    fun isValidFolderTitle() {
        // Given
        val titleMustNotBeEmptyText = "text1"
        val titleMustNotContainSlashText = "text2"
        val model = createInstance(
            titleMustNotBeEmptyText = titleMustNotBeEmptyText,
            titleMustNotContainSlashText = titleMustNotContainSlashText
        )

        // When / then
        assertThat(model.isValidFolderTitle("title")).isEqualTo(Valid)
        assertThat(model.isValidFolderTitle("")).isEqualTo(Invalid(titleMustNotBeEmptyText))
        assertThat(model.isValidFolderTitle("")).isEqualTo(Invalid(titleMustNotBeEmptyText))
        assertThat(model.isValidFolderTitle("tit/le")).isEqualTo(
            Invalid(
                titleMustNotContainSlashText
            )
        )
    }

    @Test
    fun getNavigationItems() {
        // Given
        every { treeIndex.getNodes(path) }.returns(
            Observable.fromIterable(
                listOf(
                    TINote(aggId1, null, path, title),
                    TIFolder(aggId2, null, path, title)
                ).withIndex()
            )
        )
        val model = createInstance()
        val observer: TestObserver<List<NavigationItem>> = model.getNavigationItems(path).test()

        // When

        // Then
        assertThat(observer.values().toList()).isEqualTo(
            listOf(
                listOf(
                    NINote(aggId1, path, title, false),
                    NIFolder(aggId2, path, title, false)
                ),
            )
        )
    }

    @Test
    fun `select notes`() {
        // Given
        val treeIndexNode1 = TINote(aggId1, null, path, title)
        val treeIndexNode2 = TINote(aggId2, null, path, title)
        val navigationItemNode1 =
            NINote(treeIndexNode1.aggId, treeIndexNode1.path, treeIndexNode1.title, false)
        val navigationItemNode2 =
            NINote(treeIndexNode2.aggId, treeIndexNode2.path, treeIndexNode2.title, false)
        every { treeIndex.getNodes(path) }.returns(
            Observable.fromIterable(listOf(treeIndexNode1, treeIndexNode2).withIndex())
        )
        val model = createInstance()
        val navigationItemsObserver: TestObserver<List<NavigationItem>> =
            model.getNavigationItems(path).test()
        val selectionModeEnabledObserver: TestObserver<Boolean> =
            model.isSelectionModeEnabled().test()

        // When
        val result1 = model.toggleSelection(navigationItemNode1)
        val result2 = model.toggleSelection(navigationItemNode2)
        val result3 = model.toggleSelection(navigationItemNode1)

        // Then
        assertThat(listOf(result1, result2, result3)).isEqualTo(listOf(true, true, true))
        assertThat(navigationItemsObserver.values().toList()).isEqualTo(
            listOf(
                listOf(unselected(navigationItemNode1), unselected(navigationItemNode2)),
                listOf(selected(navigationItemNode1), unselected(navigationItemNode2)),
                listOf(selected(navigationItemNode1), selected(navigationItemNode2)),
                listOf(unselected(navigationItemNode1), selected(navigationItemNode2)),
            )
        )
        assertThat(selectionModeEnabledObserver.values().toList()).isEqualTo(
            listOf(
                false,
                true
            )
        )
    }

    @Test
    fun `deselect notes`() {
        // Given
        val treeIndexNode1 = TINote(aggId1, null, path, title)
        val treeIndexNode2 = TINote(aggId2, null, path, title)
        val navigationItemNode1 =
            NINote(treeIndexNode1.aggId, treeIndexNode1.path, treeIndexNode1.title, false)
        val navigationItemNode2 =
            NINote(treeIndexNode2.aggId, treeIndexNode2.path, treeIndexNode2.title, false)
        every { treeIndex.getNodes(path) }.returns(
            Observable.fromIterable(listOf(treeIndexNode1, treeIndexNode2).withIndex())
        )
        val model = createInstance()
        model.toggleSelection(navigationItemNode1)
        model.toggleSelection(navigationItemNode2)
        val navigationItemsObserver: TestObserver<List<NavigationItem>> =
            model.getNavigationItems(path).test()
        val selectionModeEnabledObserver: TestObserver<Boolean> =
            model.isSelectionModeEnabled().test()

        // When
        val result1 = model.clearSelection()
        val result2 = model.clearSelection()

        // Then
        assertThat(listOf(result1, result2)).isEqualTo(listOf(true, false))
        assertThat(navigationItemsObserver.values().toList()).isEqualTo(
            listOf(
                listOf(selected(navigationItemNode1), selected(navigationItemNode2)),
                listOf(unselected(navigationItemNode1), unselected(navigationItemNode2)),
            )
        )
        assertThat(selectionModeEnabledObserver.values().toList()).isEqualTo(
            listOf(
                true,
                false
            )
        )
    }


    private fun createInstance(
        newNoteTitle: String = "",
        titleMustNotBeEmptyText: String = "",
        titleMustNotContainSlashText: String = ""
    ) =
        NavigationViewModel(
            commandBus,
            treeIndex,
            newNoteTitle,
            titleMustNotBeEmptyText,
            titleMustNotContainSlashText
        )

    private fun selected(note: NINote) =
        NINote(note.id, note.path, note.title, isSelected = true)

    private fun unselected(note: NINote) =
        NINote(note.id, note.path, note.title, isSelected = false)
}
