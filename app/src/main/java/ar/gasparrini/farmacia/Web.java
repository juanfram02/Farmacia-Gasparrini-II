package ar.gasparrini.farmacia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The pharmacy is the same web page as on claude.ai, bundled inside the app (works offline).
 * It is served from a fake https address so ES modules and localStorage behave like on a real site;
 * the app and the wallpaper share that address, so they share the saved settings too.
 */
final class Web {
    static final String HOST = "appassets.androidplatform.net";
    static final String URL = "https://" + HOST + "/web/index.html";

    private Web() {}

    @SuppressLint("SetJavaScriptEnabled")
    static WebView create(Context ctx) {
        WebView w = new WebView(ctx);
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        w.setBackgroundColor(0xFF120D0A);
        w.setVerticalScrollBarEnabled(false);
        w.setHorizontalScrollBarEnabled(false);
        w.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        w.setWebViewClient(new AssetClient(ctx.getApplicationContext().getAssets()));
        w.setWebChromeClient(new WebChromeClient());
        return w;
    }

    private static final class AssetClient extends WebViewClient {
        private final AssetManager assets;

        AssetClient(AssetManager assets) { this.assets = assets; }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            if (!HOST.equals(url.getHost())) return empty(404, "Offline");   // no internet needed (fonts fall back)
            String path = url.getPath();
            if (path == null || path.equals("/")) path = "/web/index.html";
            try {
                InputStream in = assets.open(path.substring(1));
                String mime = mime(path);
                return new WebResourceResponse(mime, mime.startsWith("text/") ? "utf-8" : null, in);
            } catch (IOException e) {
                return empty(404, "Not found");
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return !HOST.equals(request.getUrl().getHost());
        }

        private static WebResourceResponse empty(int code, String reason) {
            return new WebResourceResponse("text/plain", "utf-8", code, reason, null, new ByteArrayInputStream(new byte[0]));
        }

        private static String mime(String p) {
            if (p.endsWith(".html")) return "text/html";
            if (p.endsWith(".js") || p.endsWith(".mjs")) return "text/javascript";
            if (p.endsWith(".css")) return "text/css";
            if (p.endsWith(".json")) return "application/json";
            if (p.endsWith(".png")) return "image/png";
            return "application/octet-stream";
        }
    }
}
