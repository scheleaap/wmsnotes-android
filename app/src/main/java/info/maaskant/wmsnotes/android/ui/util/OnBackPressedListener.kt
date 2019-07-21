package info.maaskant.wmsnotes.android.ui.util

interface OnBackPressedListener {
    /**
     * @return A boolean indicating whether the back press was handled by the listener. If not, the back press will be
     * passed on or handled by the owner.
     */
    fun onBackPressed(): Boolean
}
