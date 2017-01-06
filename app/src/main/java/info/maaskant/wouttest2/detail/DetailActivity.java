package info.maaskant.wouttest2.detail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import info.maaskant.wouttest2.R;
import timber.log.Timber;

public class DetailActivity extends AppCompatActivity {

    public static final String PATH_KEY = "path";

    private File path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("Creating activity");

        setContentView(R.layout.activity_markdown);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(PATH_KEY)) {
            path = new File(getIntent().getStringExtra(PATH_KEY));
        } else {
            Timber.e("No file specified");
            finish();
        }
        Timber.v("Using path " + path);

        List<Extension> extensions = Arrays.asList(TablesExtension.create(), AutolinkExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

        try {
            String markdownContent = Files.toString(path, Charsets.UTF_8);
            Node document = parser.parse(markdownContent);
            String htmlContent = renderer.render(document);

            WebView webView = (WebView) findViewById(R.id.content);
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.loadData(htmlContent, "text/html", "UTF-8");
        } catch (IOException e) {
            Timber.e("Could not read file %s", path.toString(), e);
        }
    }

}