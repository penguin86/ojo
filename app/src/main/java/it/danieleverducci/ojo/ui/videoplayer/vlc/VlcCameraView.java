package it.danieleverducci.ojo.ui.videoplayer.vlc;

import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import it.danieleverducci.ojo.entities.Camera;
import it.danieleverducci.ojo.ui.SurveillanceFragment;
import it.danieleverducci.ojo.ui.videoplayer.BaseCameraView;
import it.danieleverducci.ojo.ui.videoplayer.VideoLibEnum;

/**
 * Contains all entities (views and java entities) related to a camera stream viewer
 */
public class VlcCameraView extends BaseCameraView {
    public MediaPlayer mediaPlayer;
    public IVLCVout ivlcVout;
    public LibVLC libvlc;

    public VlcCameraView(FragmentActivity context, Camera camera) {
        super(context, camera);
        surfaceView = new SurfaceView(context);
        kind = VideoLibEnum.VLC;
        this.libvlc = VlcConfig.getInstance().getLibVlc(context);
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
        m.setHWDecoderEnabled(true, false);
        mediaPlayer.setMedia(m);

        // Register for view resize events
        final ViewTreeObserver observer = surfaceView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(() -> {
            // Set rendering size
            ivlcVout.setWindowSize(surfaceView.getWidth(), surfaceView.getHeight());
        });
    }

    /**
     * Starts the playback.
     */
    @Override
    public void startPlayback() {
        mediaPlayer.play();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void resume() {
        startPlayback();
    }

    @Override
    public void stop() {
        destroy();
    }

    /**
     * Destroys the object and frees the memory
     */
    @Override
    public void destroy() {
        if (libvlc == null) {
            Log.e(SurveillanceFragment.TAG, this.toString() + " already destroyed");
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

    @Override
    public void fullScreen(@Nullable FullEvent fullEvent) {
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullEvent != null) {
                    fullEvent.fullOrNot(VlcCameraView.this);
                }
            }
        });
    }

}
