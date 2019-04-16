package info.maaskant.wmsnotes.android.ui.detail

/**
 * Cam be implemented by [Fragment]s within [DetailActivity]'s [androidx.viewpager.widget.ViewPager] to be notified when
 * the page they are in is selected.
 */
interface OnPageSelectedListener {
    /**
     * Called if the page associated with the listener is selected.
     */
    fun onPageSelected()
}
