package info.maaskant.wmsnotes.android.ui.navigation

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import info.maaskant.wmsnotes.model.Path

sealed interface NavigationItem {
    val id: String
    val isSelected: Boolean

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

class Folder(
    val aggId: String,
    val path: Path,
    val title: String,
    override val isSelected: Boolean
) :
    NavigationItem {
    override val id = aggId

    override fun equals(other: Any?) = kotlinEquals(other, properties)
    override fun toString() = kotlinToString(properties)
    override fun hashCode() = kotlinHashCode(properties)

    companion object {
        private val properties = arrayOf(
            Folder::aggId,
            Folder::path,
            Folder::title,
            Folder::isSelected
        )
    }
}

class Note(
    val aggId: String,
    val path: Path,
    val title: String,
    override val isSelected: Boolean
) :
    NavigationItem {
    override val id = aggId

    override fun equals(other: Any?) = kotlinEquals(other, properties)
    override fun toString() = kotlinToString(properties)
    override fun hashCode() = kotlinHashCode(properties)

    companion object {
        private val properties = arrayOf(
            Note::aggId,
            Note::path,
            Note::title,
            Note::isSelected
        )
    }
}
