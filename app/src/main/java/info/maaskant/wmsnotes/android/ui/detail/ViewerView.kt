package info.maaskant.wmsnotes.android.ui.detail

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import info.maaskant.wmsnotes.R
import timber.log.Timber

import java.util.Objects.requireNonNull

/**
 * A [View] that shows a rendered node.
 *
 * The node is displayed using a [android.webkit.WebView].
 */
internal class ViewerView : RelativeLayout {

    private var progressBar: ProgressBar? = null

    private var webView: WebView? = null

    constructor(context: Context) : super(context, null)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onFinishInflate() {
        super.onFinishInflate()

        progressBar = findViewById<View>(R.id.viewer_progress) as ProgressBar

        webView = findViewById<View>(R.id.viewer_webview) as WebView
        webView!!.setBackgroundColor(Color.TRANSPARENT)
        // webView.setWebChromeClient(new WebChromeClient() {
        // @Override
        // public void onProgressChanged(WebView view, int newProgress) {
        // if (newProgress == 100) {
        // // Hide the progressbar
        // progressBar.setVisibility(View.GONE);
        // }
        // }
        // });

        clear()
        // progressBar.setVisibility(GONE);
    }

    private fun clear() {
        Timber.v("Clearing")
        // progressBar.setVisibility(VISIBLE);
        webView!!.visibility = View.GONE
    }

    private fun setContent(htmlContent: String) {
        Timber.v("Setting content")
        requireNonNull(htmlContent)
        webView!!.visibility = View.VISIBLE
        webView!!.post {
            webView!!.loadDataWithBaseURL(
                "file:///android_asset/", htmlContent, "text/html",
                "UTF-8", null
            )
        }

    }

}
