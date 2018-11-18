package info.maaskant.wmsnotes.android.ui.detail

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

internal class DetailPagerAdapter(
    fragmentManager: FragmentManager,
    private val editorFragment: EditorFragment,
    private val viewerFragment: ViewerFragment
) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return if (isEditorPage(position)) {
            editorFragment
        } else {
            viewerFragment
        }
    }

    override fun getCount(): Int = 2
    fun isEditorPage(position: Int): Boolean = position == 1
    fun isViewerPage(position: Int): Boolean = position != 1
}
