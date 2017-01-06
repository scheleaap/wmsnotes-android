package info.maaskant.wouttest2.detail;

import android.support.annotation.NonNull;

import org.commonmark.node.CustomNode;

/**
 * A {@link org.commonmark.node.Node} that represents a link to a stylesheet. Only relevant for HTML
 * renderers, of course.
 */
class StylesheetNode extends CustomNode {

    private final String url;

    /**
     * Constructor.
     *
     * @param url
     *            The URL of the stylesheet.
     */
    public StylesheetNode(@NonNull final String url) {
        this.url = url;
    }

    /**
     * Returns the URL of the stylesheet.
     *
     * @return The URL of the stylesheet.
     */
    public String getUrl() {
        return url;
    }
}
