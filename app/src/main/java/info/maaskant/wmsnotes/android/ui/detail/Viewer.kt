package info.maaskant.wmsnotes.android.ui.detail

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class Viewer : WebView {
    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
}
