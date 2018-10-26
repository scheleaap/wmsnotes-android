package info.maaskant.wmsnotes.android.model

sealed class Node(val title: String)
class Folder(title: String): Node(title)
class Note(title: String): Node(title)
