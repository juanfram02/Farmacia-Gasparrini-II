package ar.gasparrini.farmacia;

import android.app.Presentation;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;

/**
 * Live wallpaper: the same page runs in a WebView that is shown on a private virtual display whose
 * output is the wallpaper's own surface. The page hides its panels in this mode, renders at half the
 * frame rate and stops completely while the wallpaper isn't visible.
 */
public class FarmaciaWallpaperService extends WallpaperService {
    private static final String TAG = "FarmaciaWallpaper";

    @Override
    public Engine onCreateEngine() {
        return new FarmEngine();
    }

    private final class FarmEngine extends Engine {
        private VirtualDisplay display;
        private Presentation presentation;
        private WindowManager windowManager;
        private WebView web;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            setTouchEventsEnabled(false);
            setOffsetNotificationsEnabled(true);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            if (width <= 0 || height <= 0) return;
            int dpi = getResources().getDisplayMetrics().densityDpi;
            if (display == null) {
                DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
                display = dm.createVirtualDisplay("farmacia-fondo", width, height, dpi, holder.getSurface(), 0);
                if (display != null) attachPage(display.getDisplay());
            } else {
                display.setSurface(holder.getSurface());
                display.resize(width, height, dpi);
            }
        }

        private void attachPage(Display target) {
            try {
                presentation = new Presentation(FarmaciaWallpaperService.this, target);
                web = Web.create(presentation.getContext());
                presentation.setContentView(web);
                presentation.show();
            } catch (Exception e) {
                Log.w(TAG, "Presentation failed, adding the page as a window", e);
                presentation = null;
                try {
                    Context dc = createDisplayContext(target);
                    Context wc = Build.VERSION.SDK_INT >= 30
                            ? dc.createWindowContext(WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION, null)
                            : dc;
                    windowManager = (WindowManager) wc.getSystemService(Context.WINDOW_SERVICE);
                    web = Web.create(wc);
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                            PixelFormat.OPAQUE);
                    windowManager.addView(web, lp);
                } catch (Exception e2) {
                    Log.e(TAG, "Could not show the pharmacy on the wallpaper", e2);
                    web = null;
                    return;
                }
            }
            web.loadUrl(Web.URL + "?mode=wallpaper");
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (web == null) return;
            if (visible) {
                web.onResume();
                js("window.__wpVisible && window.__wpVisible(true)");
            } else {
                js("window.__wpVisible && window.__wpVisible(false)");
                web.onPause();
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
            if (!isPreview()) js("window.__wpOffset && window.__wpOffset(" + xOffset + ")");
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            release();
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onDestroy() {
            release();
            super.onDestroy();
        }

        private void js(String code) {
            if (web != null) web.evaluateJavascript(code, null);
        }

        private void release() {
            if (presentation != null) {
                try { presentation.dismiss(); } catch (Exception ignored) { }
                presentation = null;
            }
            if (windowManager != null && web != null) {
                try { windowManager.removeView(web); } catch (Exception ignored) { }
                windowManager = null;
            }
            if (web != null) {
                web.destroy();
                web = null;
            }
            if (display != null) {
                display.release();
                display = null;
            }
        }
    }
}
