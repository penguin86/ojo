package it.danieleverducci.ojo.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.Settings;
import it.danieleverducci.ojo.databinding.FragmentSurveillanceBinding;
import it.danieleverducci.ojo.entities.Camera;
import it.danieleverducci.ojo.utils.DpiUtils;

/**
 * Some streams to test:
 * rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov
 * rtsp://demo:demo@ipvmdemo.dyndns.org:5541/onvif-media/media.amp?profile=profile_1_h264&sessiontimeout=60&streamtype=unicast
 */
public class SurveillanceFragment extends Fragment {

    final static private String TAG = "SurveillanceFragment";
    final static private String[] VLC_OPTIONS = new String[]{
            "--aout=opensles",
            //"--audio-time-stretch", // time stretching
            //"-vvv", // verbosity
            "--avcodec-codec=h264",
            //"--file-logging",
            //"--logfile=vlc-log.txt"
    };

    private FragmentSurveillanceBinding binding;
    private List<CameraView> cameraViews = new ArrayList<>();
    private boolean fullscreenCameraView = false;
    private LinearLayout.LayoutParams cameraViewLayoutParams;
    private LinearLayout.LayoutParams rowLayoutParams;
    private LinearLayout.LayoutParams hiddenLayoutParams;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        int viewMargin = DpiUtils.DpToPixels(container.getContext(), 2);
        cameraViewLayoutParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        );
        cameraViewLayoutParams.setMargins(viewMargin,viewMargin,viewMargin,viewMargin);

        rowLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );

        // 1,1 instead of 0,0 because the latter doesn't work on android 13+
        hiddenLayoutParams = new LinearLayout.LayoutParams(1, 1);

        binding = FragmentSurveillanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        leanbackMode(true);

        fullscreenCameraView = false;
        addAllCameras();

        // Start playback for all streams
        for (CameraView cv : cameraViews) {
            cv.startPlayback();
        }

        // Register for back pressed events
        ((MainActivity)getActivity()).setOnBackButtonPressedListener(new OnBackButtonPressedListener() {
            @Override
            public boolean onBackPressed() {
                if(fullscreenCameraView && cameraViews.size() > 1) {
                    fullscreenCameraView = false;
                    showAllCameras();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Goes fullscreen igoring the device screen insets (camera etc)
     */
    private void leanbackMode(boolean leanback) {
        Window w = requireActivity().getWindow();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
            return;

        if (leanback) {
            // Iterface can go below notch
            /*w.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );*/
            w.getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

            // Hide system bar
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(w, w.getDecorView());
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            // System bar is hidden when not touched for a while
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            // Interface cannot go below notch
            /*w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;*/

            // Show system bar
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(w, w.getDecorView());
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        leanbackMode(false);

        disposeAllCameras();
    }


    private void addAllCameras() {
        Settings settings = Settings.fromDisk(getContext());
        List<Camera> cc = settings.getCameras();

        int[] gridSize = calcGridDimensionsBasedOnNumberOfElements(cc.size());
        int camIdx = 0;
        for (int r = 0; r < gridSize[0]; r++) {
            // Create row and add to row container
            LinearLayout row = new LinearLayout(getContext());
            binding.gridRowContainer.addView(row, rowLayoutParams);
            // Add camera viewers to the row
            for (int c = 0; c < gridSize[1]; c++) {
                if ( camIdx < cc.size() ) {
                    Camera cam = cc.get(camIdx);
                    CameraView cv = addCameraView(cam, row);
                    cv.startPlayback();
                    cv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Toggle single/multi camera views
                            fullscreenCameraView = !fullscreenCameraView;
                            if (fullscreenCameraView) {
                                hideAllCameraViewsButNot(v);
                            } else {
                                showAllCameras();
                            }
                        }
                    });
                } else {
                    // Cameras are less than the maximum number of cells in grid: fill remaining cells with empty views
                    View ev = new View(getContext());
                    ev.setBackgroundColor(getResources().getColor(R.color.purple_700));
                    row.addView(ev, cameraViewLayoutParams);
                }
                camIdx++;
            }
        }
    }

    private void disposeAllCameras() {
        // Destroy players, libs etc
        for (CameraView cv : cameraViews) {
            cv.destroy();
        }
        cameraViews.clear();
        // Remove views
        binding.gridRowContainer.removeAllViews();
    }

    protected void hideAllCameraViewsButNot(View cameraView) {
        for (int i = 0; i < binding.gridRowContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) binding.gridRowContainer.getChildAt(i);
            boolean emptyRow = true;
            for (int j = 0; j < row.getChildCount(); j++) {
                View cam = row.getChildAt(j);
                if (cameraView == cam)
                    emptyRow = false;
                else
                    cam.setLayoutParams(hiddenLayoutParams);
            }
            if (emptyRow)
                row.setLayoutParams(hiddenLayoutParams);
        }
    }

    protected void showAllCameras() {
        for (int i = 0; i < binding.gridRowContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) binding.gridRowContainer.getChildAt(i);
            row.setLayoutParams(rowLayoutParams);
            for (int j = 0; j < row.getChildCount(); j++) {
                View cam = row.getChildAt(j);
                cam.setLayoutParams(cameraViewLayoutParams);
            }
        }
    }

    private CameraView addCameraView(Camera camera, LinearLayout rowContainer) {
        CameraView cv = new CameraView(
                getContext(),
                camera
        );

        // Add to layout
        rowContainer.addView(cv.surfaceView, cameraViewLayoutParams);

        cameraViews.add(cv);
        return cv;
    }

    /**
     * Returns the dimensions of the grid based on the number of elements.
     * Es: to display 3 elements is needed a 4-element grid, with 2 elements per side (a 2x2 grid)
     * Es: to display 6 elements is needed a 9-element grid, with 3 elements per side (a 2x3 grid)
     * Es: to display 7 elements is needed a 9-element grid, with 3 elements per side (a 3x3 grid)
     * @param elements
     */
    private int[] calcGridDimensionsBasedOnNumberOfElements(int elements) {
        int rows = 1;
        int cols = 1;
        while (rows * cols < elements) {
            cols += 1;
            if (rows * cols >= elements) break;
            rows += 1;
        }
        int[] dimensions = {rows, cols};
        return dimensions;
    }

    /**
     * Contains all entities (views and java entities) related to a camera stream viewer
     */
    private class CameraView {
        protected SurfaceView surfaceView;
        protected MediaPlayer mediaPlayer;
        protected IVLCVout ivlcVout;
        protected Camera camera;
        protected LibVLC libvlc;

        public CameraView(Context context, Camera camera) {
            this.camera = camera;
            this.libvlc = new LibVLC(context, new ArrayList<>(Arrays.asList(VLC_OPTIONS)));

            surfaceView = new SurfaceView(context);
            surfaceView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            SurfaceHolder holder = surfaceView.getHolder();

            holder.setKeepScreenOn(true);

            // Create media player
            mediaPlayer = new MediaPlayer(libvlc);

            // Set up video output
            ivlcVout = mediaPlayer.getVLCVout();
            ivlcVout.setVideoView(surfaceView);
            ivlcVout.attachViews();

            // Load media and start playing
            Media m = new Media(libvlc, Uri.parse(camera.getRtspUrl()));
            mediaPlayer.setMedia(m);

            // Register for view resize events
            final ViewTreeObserver observer= surfaceView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(() -> {
                // Set rendering size
                ivlcVout.setWindowSize(surfaceView.getWidth(), surfaceView.getHeight());
            });
        }

        public void setOnClickListener(View.OnClickListener listener) {
            surfaceView.setOnClickListener(listener);
        }

        /**
         * Starts the playback.
         */
        public void startPlayback() {
            mediaPlayer.play();
        }

        /**
         * Destroys the object and frees the memory
         */
        public void destroy() {
            if (libvlc == null) {
                Log.e(TAG, this.toString() + " already destroyed");
                return;
            }

            mediaPlayer.stop();
            final IVLCVout vout = mediaPlayer.getVLCVout();
            vout.detachViews();
            libvlc.release();
            libvlc = null;
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}