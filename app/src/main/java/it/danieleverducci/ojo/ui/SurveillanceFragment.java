package it.danieleverducci.ojo.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;

import java.util.ArrayList;
import java.util.List;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.Settings;
import it.danieleverducci.ojo.SharedPreferencesManager;
import it.danieleverducci.ojo.databinding.FragmentSurveillanceBinding;
import it.danieleverducci.ojo.entities.Camera;
import it.danieleverducci.ojo.ui.videoplayer.BaseCameraView;
import it.danieleverducci.ojo.ui.videoplayer.vlc.VlcCameraView;
import it.danieleverducci.ojo.ui.videoplayer.gsy.GsyCameraView;
import it.danieleverducci.ojo.ui.videoplayer.VideoLibEnum;
import it.danieleverducci.ojo.utils.DpiUtils;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;

/**
 * Some streams to test:
 * rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov
 * rtsp://demo:demo@ipvmdemo.dyndns.org:5541/onvif-media/media.amp?profile=profile_1_h264&sessiontimeout=60&streamtype=unicast
 */
public class SurveillanceFragment extends Fragment {
    public static VideoLibEnum videoLibEnum = VideoLibEnum.EXO;

    public final static String TAG = "SurveillanceFragment";

    private FragmentSurveillanceBinding binding;
    private final List<BaseCameraView> cameraViews = new ArrayList<>();
    private boolean fullscreenCameraView = false;
    private LinearLayout.LayoutParams cameraViewLayoutParams;
    private LinearLayout.LayoutParams rowLayoutParams;
    private LinearLayout.LayoutParams hiddenLayoutParams;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        int viewMargin = DpiUtils.DpToPixels(requireContext(), 2);
        cameraViewLayoutParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        );
        cameraViewLayoutParams.setMargins(viewMargin, viewMargin, viewMargin, viewMargin);

        rowLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );

        hiddenLayoutParams = new LinearLayout.LayoutParams(0, 0);

        binding = FragmentSurveillanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        setUseWhichVideoPlayer();
    }

    private void setUseWhichVideoPlayer() {
        int whichlib = SharedPreferencesManager.useWhichLib(requireContext());
        if (whichlib == VideoLibEnum.EXO.i) {
            videoLibEnum = VideoLibEnum.EXO;
            PlayerFactory.setPlayManager(Exo2PlayerManager.class);
        } else if (whichlib == VideoLibEnum.VLC.i) {
            videoLibEnum = VideoLibEnum.VLC;
        } else if (whichlib == VideoLibEnum.IJK.i) {
            videoLibEnum = VideoLibEnum.IJK;
            PlayerFactory.setPlayManager(IjkPlayerManager.class);
        } else if (whichlib == VideoLibEnum.SYSTEM.i) {
            videoLibEnum = VideoLibEnum.SYSTEM;
            PlayerFactory.setPlayManager(SystemPlayerManager.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Leanback mode (fullscreen)
        Window window = requireActivity().getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                final WindowInsetsController controller = window.getInsetsController();

                if (controller != null)
                    controller.hide(WindowInsets.Type.statusBars());
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            }
        }

        fullscreenCameraView = false;
        addAllCameras();

        // Start playback for all streams
        for (BaseCameraView cv : cameraViews) {
            cv.startPlayback();
        }

        // Register for back pressed events
        ((MainActivity) requireActivity()).setOnBackButtonPressedListener(new OnBackButtonPressedListener() {
            @Override
            public boolean onBackPressed() {
                if (fullscreenCameraView && cameraViews.size() > 1) {
                    fullscreenCameraView = false;
                    showAllCameras();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // Disable Leanback mode (fullscreen)
        Window window = requireActivity().getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                final WindowInsetsController controller = window.getInsetsController();

                if (controller != null)
                    controller.show(WindowInsets.Type.statusBars());
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }

        disposeAllCameras();
    }


    private void addAllCameras() {
        Settings settings = Settings.fromDisk(requireActivity());
        List<Camera> cc = settings.getEnabledCameras();

        int[] gridSize = calcGridDimensionsBasedOnNumberOfElements(cc.size());
        int camIdx = 0;
        for (int r = 0; r < gridSize[0]; r++) {
            // Create row and add to row container
            LinearLayout row = new LinearLayout(getContext());//几行
            binding.gridRowContainer.addView(row, rowLayoutParams);
            // Add camera viewers to the row
            for (int c = 0; c < gridSize[1]; c++) {
                if (camIdx < cc.size()) {
                    Camera cam = cc.get(camIdx);
                    BaseCameraView cv = addCameraView(cam, row);//几列
                    cv.startPlayback();
                    cv.fullScreen(new BaseCameraView.FullEvent() {
                        @Override
                        public void fullOrNot(BaseCameraView baseCameraView) {
                            // Toggle single/multi camera views
                            fullscreenCameraView = !fullscreenCameraView;
                            if (fullscreenCameraView) {
                                hideAllCameraViewsButNot(baseCameraView);
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
        for (BaseCameraView cv : cameraViews) {
            cv.destroy();
        }
        cameraViews.clear();
        // Remove views
        binding.gridRowContainer.removeAllViews();
    }

    public void hideAllCameraViewsButNot(BaseCameraView baseCameraView) {
        View cameraView;
        if (baseCameraView.kind == VideoLibEnum.VLC)
            cameraView = baseCameraView.surfaceView;
        else
            cameraView = baseCameraView.gsyVideoPlayer;

        for (BaseCameraView cm : cameraViews) {//stop other VideoView
            if (cm != baseCameraView) {
                cm.stop();
            }
        }

        for (int i = 0; i < binding.gridRowContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) binding.gridRowContainer.getChildAt(i);
            boolean emptyRow = true;
            for (int j = 0; j < row.getChildCount(); j++) {
                View cam = row.getChildAt(j);
                if (cameraView == cam) {
                    emptyRow = false;
                } else {
                    cam.setLayoutParams(hiddenLayoutParams);
                }
            }
            if (emptyRow)
                row.setLayoutParams(hiddenLayoutParams);
        }
    }

    public void showAllCameras() {
        for (int i = 0; i < binding.gridRowContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) binding.gridRowContainer.getChildAt(i);
            row.setLayoutParams(rowLayoutParams);
            for (int j = 0; j < row.getChildCount(); j++) {
                View cam = row.getChildAt(j);
                cam.setLayoutParams(cameraViewLayoutParams);
            }
            for (BaseCameraView cameraView : cameraViews) {
                cameraView.startPlayback();
            }
        }
    }

    /**
     * 生成vlc版本的视频播放
     */
    private VlcCameraView genVlc(Camera camera, LinearLayout rowContainer) {
        VlcCameraView cv = new VlcCameraView(requireActivity(), camera);
        // Add to layout
        rowContainer.addView(cv.surfaceView, cameraViewLayoutParams);
        return cv;
    }
    /**
     * 生成gsy版本的视频播放
     */
    private BaseCameraView genGsy(Camera camera, LinearLayout rowContainer) {
        GsyCameraView cv = new GsyCameraView(requireActivity(), camera);
        rowContainer.addView(cv.gsyVideoPlayer, cameraViewLayoutParams);
        return cv;
    }

    private BaseCameraView addCameraView(Camera camera, LinearLayout rowContainer) {
        BaseCameraView cv;
        if (videoLibEnum.i != VideoLibEnum.VLC.i) {
            cv = genGsy(camera, rowContainer);
        } else {
            cv = genVlc(camera, rowContainer);
        }
        cv.kind = videoLibEnum;
        cameraViews.add(cv);
        return cv;
    }

    /**
     * Returns the dimensions of the grid based on the number of elements.
     * Es: to display 3 elements is needed a 4-element grid, with 2 elements per side (a 2x2 grid)
     * Es: to display 6 elements is needed a 9-element grid, with 3 elements per side (a 2x3 grid)
     * Es: to display 7 elements is needed a 9-element grid, with 3 elements per side (a 3x3 grid)
     *
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
        return new int[]{rows, cols};
    }
}