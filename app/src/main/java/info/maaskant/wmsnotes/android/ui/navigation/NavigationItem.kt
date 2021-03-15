package info.maaskant.wmsnotes.android.ui.navigation

import au.com.console.kassava.kotlinEquals
import info.maaskant.wmsnotes.model.Path

sealed interface NavigationItem {
    val id: String
    val path: Path
    val title: String
    val isSelected: Boolean

    fun equalsIgnoringSelection(other: Any?) = kotlinEquals(
        other, arrayOf(
            NavigationItem::id,
            NavigationItem::path,
            NavigationItem::title
        )
    )

    companion object {
        fun fromNode(node: info.maaskant.wmsnotes.client.indexing.Node, isSelected: Boolean) =
            when (node) {
                is info.maaskant.wmsnotes.client.indexing.Note -> Note(
                    node.aggId,
                    node.path,
                    node.title,
                    isSelected
                )
                is info.maaskant.wmsnotes.client.indexing.Folder -> Folder(
                    node.aggId,
                    node.path,
                    node.title,
                    isSelected
                )
            }
    }
}

data class Folder(
    override val id: String,
    override val path: Path,
    override val title: String,
    override val isSelected: Boolean
) : NavigationItem

data class Note(
    override val id: String,
    override val path: Path,
    override val title: String,
    override val isSelected: Boolean
) : NavigationItem
