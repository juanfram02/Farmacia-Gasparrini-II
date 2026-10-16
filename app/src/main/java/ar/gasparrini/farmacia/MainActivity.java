package ar.gasparrini.farmacia;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

/** The pharmacy full screen, with all its controls, plus the button that sets it as the wallpaper. */
public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = Web.create(this);
        web.addJavascriptInterface(new Bridge(), "Android");
        setContentView(web);
        web.loadUrl(Web.URL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }

    private void openWallpaperPicker() {
        try {
            Intent i = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, FarmaciaWallpaperService.class));
            startActivity(i);
        } catch (Exception e) {
            try {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
            } catch (Exception e2) {
                Toast.makeText(this, R.string.wallpaper_fallback, Toast.LENGTH_LONG).show();
            }
        }
    }

    /** Called from the page (window.Android). */
    public final class Bridge {
        @JavascriptInterface
        public void setWallpaper() {
            runOnUiThread(MainActivity.this::openWallpaperPicker);
        }
    }
}
