package it.danieleverducci.ojo.ui.videoplayer.gsy;

import static it.danieleverducci.ojo.ui.videoplayer.gsy.CustomManager.random;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;

import java.util.UUID;

import it.danieleverducci.ojo.R;
import it.danieleverducci.ojo.entities.Camera;
import it.danieleverducci.ojo.ui.videoplayer.BaseCameraView;
import it.danieleverducci.ojo.ui.videoplayer.VideoLibEnum;

public class GsyCameraView extends BaseCameraView implements View.OnClickListener {
    private FullEvent fullEvent = null;


    public GsyCameraView(FragmentActivity context, Camera camera) {
        super(context, camera);
        kind = VideoLibEnum.EXO;
        gsyVideoPlayer = new MultiSampleVideo(context);
        //gsyVideoPlayer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption
                .setIsTouchWiget(false)
                .setRotateViewAuto(true)
                .setLockLand(true)
                .setAutoFullWithSize(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setUrl(camera.getRtspUrl())
                .setCacheWithPlay(false)
                .build(gsyVideoPlayer);
        gsyVideoPlayer.setPlayTag(UUID.randomUUID().toString());
        gsyVideoPlayer.setPlayPosition(random.nextInt());

    }

    @Override
    public void startPlayback() {
        gsyVideoPlayer.startPlayLogic();
    }

    @Override
    public void pause() {
        gsyVideoPlayer.onVideoPause();
    }

    @Override
    public void resume() {
        gsyVideoPlayer.onVideoResume();
    }

    @Override
    public void stop() {
        gsyVideoPlayer.release();
    }

    @Override
    public void destroy() {
        gsyVideoPlayer.release();
    }

    @Override
    public void fullScreen(@Nullable FullEvent fullEvent) {
        this.fullEvent = fullEvent;
        gsyVideoPlayer.getBackButton().setOnClickListener(this);
        gsyVideoPlayer.getFullscreenButton().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fullscreen) {
            if (fullEvent != null) {
                fullEvent.fullOrNot(GsyCameraView.this);
            }
        }
    }
}
