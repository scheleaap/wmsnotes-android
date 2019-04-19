package info.maaskant.wmsnotes.android.ui.navigation

import android.os.Bundle
import info.maaskant.wmsnotes.client.indexing.TreeIndex
import info.maaskant.wmsnotes.model.CommandProcessor
import info.maaskant.wmsnotes.model.Path
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.reactivex.observers.TestObserver
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.shouldHaveThrown
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NavigationViewModelTest {
    private val commandProcessor: CommandProcessor = mockk()

    private val treeIndex: TreeIndex = mockk()

    private val newNoteTitle: String = "new"

    @BeforeEach
    fun init() {
        clearMocks(
            commandProcessor,
            treeIndex
        )
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

    private fun createInstance() = NavigationViewModel(commandProcessor, treeIndex, newNoteTitle)
}
