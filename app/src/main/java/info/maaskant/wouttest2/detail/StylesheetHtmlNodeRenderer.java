package info.maaskant.wouttest2.detail;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;

import android.support.v4.util.ArrayMap;

/**
 * A {@link NodeRenderer} that renders {@link StylesheetNode}s.
 */
class StylesheetHtmlNodeRenderer implements NodeRenderer {

    private final HtmlWriter html;

    StylesheetHtmlNodeRenderer(HtmlNodeRendererContext context) {
        this.html = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Collections.<Class<? extends Node>> singleton(StylesheetNode.class);
    }

    @Override
    public void render(Node node) {
        render((StylesheetNode) node);
    }

    private void render(StylesheetNode node) {
        Map<String, String> attributes = new ArrayMap<>(3);
        attributes.put("rel", "stylesheet");
        attributes.put("type", "text/css");
        attributes.put("href", node.getUrl());
        html.tag("link", attributes, true);
        html.line();
    }

}
