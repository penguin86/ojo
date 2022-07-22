package it.danieleverducci.ojo.ui.videoplayer;

import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import it.danieleverducci.ojo.entities.Camera;

public abstract class BaseCameraView {
    public Camera camera;
    public SurfaceView surfaceView;
    public GSYVideoPlayer gsyVideoPlayer = null;
    public VideoLibEnum kind;

    public BaseCameraView(FragmentActivity context, Camera camera) {
        this.camera = camera;
    }

    /**
     * Starts the playback.
     */
    public abstract void startPlayback();

    public abstract void pause();

    public abstract void resume();

    public abstract void stop();

    /**
     * Destroys the object and frees the memory
     */
    public abstract void destroy();

    public abstract void fullScreen(@Nullable FullEvent fullEvent);

    public interface FullEvent {
        void fullOrNot(BaseCameraView cameraView);
    }

}
