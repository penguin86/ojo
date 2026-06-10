package it.danieleverducci.ojo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            // Default AudioTrack output: opensles does not support
            // software volume (setVolume/getVolume), which mute relies on
            //"--aout=opensles",
            //"--audio-time-stretch", // time stretching
            //"-vvv", // verbosity
            "--avcodec-codec=h264",
            //"--file-logging",
            //"--logfile=vlc-log.txt"
    };

    private FragmentSurveillanceBinding binding;
    private Settings settings;
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

        expandToCameraViewIfRequired();

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
            w.getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

            // Hide system bar
            WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(w, w.getDecorView());
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            // System bar is hidden when not touched for a while
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            // Show system bar
            //WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(w, w.getDecorView());
            //windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        leanbackMode(false);

        disposeAllCameras();
    }


    private void addAllCameras() {
        settings = Settings.fromDisk(getContext());
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
                    cv.container.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Toggle single/multi camera views
                            fullscreenCameraView = !fullscreenCameraView;
                            if (fullscreenCameraView) {
                                // Going fullscreen - make this view fill the screen
                                ViewGroup.LayoutParams params = cv.container.getLayoutParams();
                                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                cv.container.setLayoutParams(params);
                                hideAllCameraViewsButNot(cv.container);
                            } else {
                                // Going back to grid - restore original layout params
                                cv.container.setLayoutParams(cv.originalLayoutParams);
                                showAllCameras();
                            }
                            cv.setMuteButtonPosition(fullscreenCameraView);
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
            boolean found = false;
            for (int j = 0; j < row.getChildCount(); j++) {
                View child = row.getChildAt(j);
                if (child == cameraView) {
                    found = true;
                } else {
                    child.setVisibility(View.GONE);
                }
            }
            if (!found) {
                row.setVisibility(View.GONE);
            }
        }
    }

    protected void showAllCameras() {
        // Restore original layout parameters for all camera views
        for (CameraView cv : cameraViews) {
            cv.container.setLayoutParams(cv.originalLayoutParams);
        }

        for (int i = 0; i < binding.gridRowContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) binding.gridRowContainer.getChildAt(i);
            row.setVisibility(View.VISIBLE);
            for (int j = 0; j < row.getChildCount(); j++) {
                View child = row.getChildAt(j);
                child.setVisibility(View.VISIBLE);
            }
        }
    }

    private CameraView addCameraView(Camera camera, LinearLayout rowContainer) {
        CameraView cv = new CameraView(
                getContext(),
                camera
        );

        // Create a container FrameLayout for the SurfaceView and mute button
        FrameLayout container = new FrameLayout(getContext());
        container.setLayoutParams(cameraViewLayoutParams);
        cv.container = container;
        cv.originalLayoutParams = cameraViewLayoutParams;

        // SurfaceView for video
        cv.surfaceView = new SurfaceView(getContext());
        cv.surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle single/multi camera views
                fullscreenCameraView = !fullscreenCameraView;
                if (fullscreenCameraView) {
                    // Going fullscreen - make this view fill the screen
                    ViewGroup.LayoutParams params = cv.container.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    cv.container.setLayoutParams(params);
                    hideAllCameraViewsButNot(cv.container);
                } else {
                    // Going back to grid - restore original layout params
                    cv.container.setLayoutParams(cv.originalLayoutParams);
                    showAllCameras();
                }
                cv.setMuteButtonPosition(fullscreenCameraView);
            }
        });
        cv.surfaceView.setOnFocusChangeListener((view, hasFocus) -> view.setBackgroundResource(hasFocus ? R.drawable.focus_border : 0));
        // Set up SurfaceHolder callback so VLC reattaches after surface resize
        cv.surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (cv.mediaPlayer == null) return;
                IVLCVout vout = cv.mediaPlayer.getVLCVout();
                if (vout.areViewsAttached()) vout.detachViews();
                vout.setVideoView(cv.surfaceView);
                vout.attachViews();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (cv.mediaPlayer != null) {
                    cv.mediaPlayer.getVLCVout().setWindowSize(width, height);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (cv.mediaPlayer != null) {
                    // Detach only — do NOT stop. VLC keeps buffering internally
                    // and will resume rendering when the surface is recreated.
                    cv.mediaPlayer.getVLCVout().detachViews();
                }
            }
        });
        container.addView(cv.surfaceView);

        // Mute button
        cv.muteButton = new ImageButton(getContext());
        cv.muteButton.setBackgroundColor(0x66000000);
        cv.muteButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int btnSize = DpiUtils.DpToPixels(getContext(), 32);
        int btnPadding = DpiUtils.DpToPixels(getContext(), 4);
        cv.muteButton.setPadding(btnPadding, btnPadding, btnPadding, btnPadding);
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                btnSize, btnSize, Gravity.TOP | Gravity.START);
        cv.muteButton.setLayoutParams(btnParams);
        cv.setMuteButtonPosition(false);
        cv.updateMuteButton();
        cv.muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cv.hasAudio) {
                    cv.isMuted = !cv.isMuted;
                    // Software volume: unlike setAudioTrack(-1), this doesn't
                    // shut down the audio output, so it can be toggled back on
                    cv.mediaPlayer.setVolume(cv.isMuted ? 0 : 100);
                    cv.updateMuteButton();
                    // Persist mute state across restarts
                    cv.camera.setMuted(cv.isMuted);
                    settings.save();
                }
            }
        });
        container.addView(cv.muteButton);

        // Create media player
        cv.mediaPlayer = new MediaPlayer(cv.libvlc);

        // Listen for ES (elementary stream) added events to detect audio tracks
        cv.mediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                if (event.type == MediaPlayer.Event.ESAdded) {
                    if (cv.mediaPlayer.getAudioTracksCount() > 0 && !cv.hasAudio) {
                        cv.hasAudio = true;
                        cv.audioTrackId = cv.mediaPlayer.getAudioTrack();
                        if (cv.camera.isMuted()) {
                            cv.isMuted = true;
                            cv.muteButton.post(() -> cv.applyMuteWithRetry(0));
                        }
                        cv.muteButton.post(() -> cv.updateMuteButton());
                    }
                }
            }
        });

        // Load media
        Media m = new Media(cv.libvlc, Uri.parse(camera.getRtspUrl()));
        cv.mediaPlayer.setMedia(m);

        cameraViews.add(cv);
        rowContainer.addView(container);
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

    private void expandToCameraViewIfRequired() {
        final String EXTRA_CAMERA_NUMBER = "it.danieleverducci.ojo.CAMERA_NUMBER";
        final String EXTRA_CAMERA_NAME = "it.danieleverducci.ojo.CAMERA_NAME";
        final String OPEN_CAMERA = "it.danieleverducci.ojo.OPEN_CAMERA";

        if (this.getActivity() == null) {
            return;
        }

        Intent intent = this.getActivity().getIntent();

        if (OPEN_CAMERA.equals(intent.getAction())) {
            String cameraName = intent.getStringExtra(EXTRA_CAMERA_NAME);
            if (cameraName == null) {
                int cameraNumber = intent.getIntExtra(EXTRA_CAMERA_NUMBER, 0) - 1;
                expandByIndex(cameraNumber);
                return;
            }
            expandByName(cameraName);
        }
    }

    private void expandByIndex(int index) {
        if (index < 0 || cameraViews.size() <= index) {
            return;
        }
        // Going fullscreen - make this view fill the screen
        CameraView cv = cameraViews.get(index);
        ViewGroup.LayoutParams params = cv.container.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        cv.container.setLayoutParams(params);
        hideAllCameraViewsButNot(cv.container);
    }

    private void expandByName(String name) {
        for(CameraView cameraView: cameraViews) {
            if (cameraView.camera.getName().equals(name)) {
                // Going fullscreen - make this view fill the screen
                ViewGroup.LayoutParams params = cameraView.container.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                cameraView.container.setLayoutParams(params);
                hideAllCameraViewsButNot(cameraView.container);
                break;
            }
        }
    }

    /**
     * Contains all entities (views and java entities) related to a camera stream viewer
     */
    private class CameraView {
        protected SurfaceView surfaceView;
        protected MediaPlayer mediaPlayer;
        protected Camera camera;
        protected LibVLC libvlc;
        protected ImageButton muteButton;
        protected boolean hasAudio = false;
        protected boolean isMuted = false;
        protected int audioTrackId = -1;
        protected FrameLayout container;
        protected ViewGroup.LayoutParams originalLayoutParams;
        protected boolean playbackStarted = false;

        public CameraView(Context context, Camera camera) {
            this.camera = camera;
            this.libvlc = new LibVLC(context, new ArrayList<>(Arrays.asList(VLC_OPTIONS)));
        }

        private void updateMuteButton() {
            if (!hasAudio) {
                muteButton.setImageResource(R.drawable.ic_music_off);
                muteButton.setColorFilter(Color.GRAY); // No audio available
            } else if (isMuted) {
                muteButton.setImageResource(R.drawable.ic_music_off);
                muteButton.setColorFilter(Color.RED); // Muted
            } else {
                muteButton.setImageResource(R.drawable.ic_music_note);
                muteButton.setColorFilter(Color.GREEN); // Playing with audio
            }
        }

        /**
         * Moves the mute button: in fullscreen it is shifted down and right
         * to clear curved screen corners.
         */
        protected void setMuteButtonPosition(boolean fullscreen) {
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) muteButton.getLayoutParams();
            int margin = DpiUtils.DpToPixels(muteButton.getContext(), fullscreen ? 24 : 4);
            p.setMargins(margin, margin, 0, 0);
            muteButton.setLayoutParams(p);
        }

        /**
         * Applies the persisted mute state. The audio output may not be
         * initialized yet when the audio track is first detected, in which
         * case setVolume doesn't take effect: verify and retry.
         */
        protected void applyMuteWithRetry(int attempt) {
            if (mediaPlayer == null || !isMuted) return;
            mediaPlayer.setVolume(0);
            if (mediaPlayer.getVolume() != 0 && attempt < 10) {
                muteButton.postDelayed(() -> applyMuteWithRetry(attempt + 1), 300);
            }
        }

        /**
         * Starts the playback.
         */
        public void startPlayback() {
            playbackStarted = true;
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

            playbackStarted = false;
            mediaPlayer.stop();
            mediaPlayer.getVLCVout().detachViews();
            libvlc.release();
            libvlc = null;
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}