package info.maaskant.wmsnotes.android.ui.navigation

class ImmutableStack<T> private constructor(private val _items: List<T>) {
    val items: List<T>
        get() = _items

    fun peek(): T? = if (_items.isNotEmpty()) {
        _items[_items.lastIndex]
    } else {
        null
    }

    fun pop(): Pair<ImmutableStack<T>, T?> {
        return if (_items.isNotEmpty()) {
            ImmutableStack(_items.dropLast(1)) to _items.last()
        } else {
            this to null
        }
    }

    fun push(value: T): ImmutableStack<T> = ImmutableStack(_items + value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImmutableStack<*>

        if (_items != other._items) return false

        return true
    }

    override fun hashCode(): Int {
        return _items.hashCode()
    }

    override fun toString(): String {
        return "ImmutableStack$_items"
    }

    companion object {
        fun <T> empty(): ImmutableStack<T> =
            ImmutableStack(emptyList<T>())

        fun <T> from(vararg items: T): ImmutableStack<T> =
            ImmutableStack(items.toMutableList())
    }
}

