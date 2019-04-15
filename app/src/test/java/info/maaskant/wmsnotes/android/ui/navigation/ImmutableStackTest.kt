package info.maaskant.wmsnotes.android.ui.navigation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ImmutableStackTest {
    @Test
    fun `initial value`() {
        // Given
        val stack = ImmutableStack.empty<Int>()

        // When

        // Then
        assertThat(stack.items).isEmpty()
    }

    @Test
    fun push() {
        // Given
        val original = ImmutableStack.empty<Int>()

        // When
        val new = original
            .push(1)
            .push(2)

        // Then
        assertThat(original).isEqualTo(ImmutableStack.empty<Int>())
        assertThat(new.items).isEqualTo(listOf(1, 2))
    }

    @Test
    fun pop() {
        // Given
        val original = ImmutableStack.empty<Int>()
            .push(1)
            .push(2)

        // When
        val (new, poppedValue) = original.pop()

        // Then
        assertThat(original).isEqualTo(ImmutableStack.from(1, 2))
        assertThat(new.items).isEqualTo(listOf(1))
        assertThat(poppedValue).isEqualTo(2)
    }

    @Test
    fun `pop, empty stack`() {
        // Given
        val original = ImmutableStack.empty<Int>()

        // When
        val (new, poppedValue) = original.pop()

        // Then
        assertThat(original).isEqualTo(ImmutableStack.empty<Int>())
        assertThat(new).isSameAs(original)
        assertThat(poppedValue).isEqualTo(null)
    }

    @Test
    fun peek() {
        // Given
        val stack = ImmutableStack.empty<Int>()
            .push(1)
            .push(2)

        // When
        val result = stack.peek()

        // Then
        assertThat(result).isEqualTo(2)
    }

    @Test
    fun `peek, empty stack`() {
        // Given
        val stack = ImmutableStack.empty<Int>()

        // When
        val result = stack.peek()

        // Then
        assertThat(result).isEqualTo(null)
    }

    @Test
    fun equals() {
        assertThat(ImmutableStack.from(1, 2, 3)).isEqualTo(ImmutableStack.from(1, 2, 3))
        assertThat(ImmutableStack.from(1, 2, 3)).isNotEqualTo(ImmutableStack.from(1, 2))
    }
}
